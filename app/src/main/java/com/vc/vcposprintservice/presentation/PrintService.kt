package com.vc.vcposprintservice.presentation

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.IBinder
import android.printservice.PrintService
import android.util.Log
import android.widget.Toast
import androidx.core.app.ServiceCompat
import com.vc.vcposprintservice.domain.usecases.GetFiles
import com.vc.vcposprintservice.domain.usecases.printer.GetPrinter
import com.vc.vcposprintservice.domain.usecases.servicestate.SaveState
import com.vc.vcposprintservice.notification.NotificationHelper
import com.vc.vcposprintservice.utils.Result
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import javax.inject.Inject


@AndroidEntryPoint
class PrintService : Service() {

    private val logger: Logger = LoggerFactory.getLogger(PrintService::class.java)

    @Inject
    lateinit var getFiles: GetFiles

    @Inject
    lateinit var getPrinter: GetPrinter

    @Inject
    lateinit var saveState: SaveState

    private val coroutineScope = CoroutineScope(Job())
    private var timerJob: Job? = null
    private var serviceJob: Job? = null
    private lateinit var manager: UsbManager
    private lateinit var usbDevice: UsbDevice
    private val forceClaim = true

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_USB_PERMISSION -> {
                    logger.info("Начало печати...")
                    startPrinting()
                }

                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    logger.info("Usb устройство подключено")
                    serviceJob = CoroutineScope(Dispatchers.IO).launch {
                        checkUsbDeviceList()
                    }
                }

                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    logger.info("USB устройство отключено")
                    logger.info("Принтер был отключен")
                    stopPrinting()
                }

                else -> {
                    logger.info("Разрешение запрещено для устройства ${usbDevice.productName}")
                }
            }
        }

    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Action.START.toString() -> start()
            Action.STOP.toString() -> stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    enum class Action {
        START, STOP
    }

    private fun start() {
        saveState(isActive = true)
        NotificationHelper.createNotificationChannel(context = this)
        ServiceCompat.startForeground(
            this,
            NotificationHelper.NOTIFICATION_ID,
            NotificationHelper.buildNotification(
                context = this,
                "PrintService начал свою работу..."
            ),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else {
                0
            }
        )

        registerUsbReceiver()

        serviceJob = CoroutineScope(Dispatchers.IO).launch {
            checkUsbDeviceList()
        }
    }

    private fun startPrinting() {
        timerJob?.cancel()
        timerJob = coroutineScope.launch {
            while (true) {
                try {
                    NotificationHelper.updateNotification(
                        context = this@PrintService,
                        contentText = "Получение файлов..."
                    )
                    when (val result = getFiles(
                        context = this@PrintService, assetsFileNames = listOf(
                            "1", "3.SPL", "17.SPL"
                        )
                    )) {
                        is Result.Error -> {
                            logger.error(result.exception.message)
                            NotificationHelper.updateNotification(
                                context = this@PrintService,
                                contentText = "Error: ${result.exception.message}"
                            )
                        }

                        is Result.Success -> setUpCommunication(
                            device = usbDevice,
                            filesName = result.data
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка во время выполнения задачи: ${e.message}", e)
                    logger.error("Ошибка во время выполнения задачи: ${e.message}")
                }

                delay(TIMER_PERIOD_MILISECOND)

            }
        }
    }

    private fun stopPrinting() {
        timerJob?.cancel()
        logger.info("Печать прекращена")
        NotificationHelper.updateNotification(this, "Печать прекращена")
    }

    private suspend fun checkUsbDeviceList() {
        manager = getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList = manager.deviceList
        when (val result = getPrinter()) {
            is Result.Error -> logger.error(result.exception.message)
            is Result.Success -> {
                val printerDevice = result.data
                deviceList.values.forEach { device ->
                    if (
                        printerDevice.productName == device.productName &&
                        printerDevice.manufactureName == device.manufacturerName
                    ) {
                        logger.info("Найдено устройство: ${device.productName}")
                        usbDevice = device
                        withContext(Dispatchers.Main) {
                            requestPermission()
                        }
                    }
                }
            }
        }

    }

    private fun registerUsbReceiver() {
        val filter = IntentFilter().apply {
            addAction(ACTION_USB_PERMISSION)
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        registerReceiver(usbReceiver, filter)
    }

    private fun unregisterUsbReceiver() {
        try {
            unregisterReceiver(usbReceiver)
        } catch (e: IllegalArgumentException) {
            logger.error("Receiver was not registered: ${e.message}")
        }
    }

    private fun requestPermission() {
        val permissionIntent = PendingIntent.getBroadcast(
            this, 0,
            Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE
        )
        manager.requestPermission(usbDevice, permissionIntent)
    }

    private suspend fun setUpCommunication(device: UsbDevice?, filesName: List<String>) {
        if (device != null && manager.hasPermission(device)) {
            device.getInterface(0).also { printerInterface ->
                printerInterface.getEndpoint(0).also { endpoint ->
                    manager.openDevice(device)?.apply {
                        claimInterface(printerInterface, forceClaim)
                        try {
                            filesName.forEachIndexed { index, fileName ->
                                NotificationHelper.updateNotification(
                                    context = this@PrintService,
                                    contentText = "Печать файла $fileName"
                                )
                                val file = File(filesDir, fileName)
                                if (!file.exists()) {
                                    logger.error("Файл с именем $fileName не существует")
                                    return
                                }
                                openFileInput(fileName).use { inputStream ->
                                    val buffer = ByteArray(4096)
                                    var bytesRead: Int
                                    while (inputStream?.read(buffer).also {
                                            bytesRead = it ?: -1
                                        } != -1) {
                                        val result =
                                            bulkTransfer(endpoint, buffer, bytesRead, TIMEOUT)
                                        if (result >= 0) {
                                            Log.d(
                                                TAG,
                                                "Данные успешно отправлены: $result байт"
                                            )
                                        } else {
                                            Log.e(TAG, "Не удалось отправить данные")
                                            logger.error("Не удалось отправить данные для файла $fileName")
                                            break
                                        }
                                    }
                                    logger.info("Файл $fileName успешно отправлен на печать")
                                    val deleted = file.delete()
                                    if (deleted) {
                                        logger.info("Файл $fileName удален")
                                        println("File $fileName deleted immediately.")
                                    } else {
                                        logger.error("Ошибка удаления файла $fileName")
                                        println("Failed $fileName to delete file.")
                                    }
                                }
                                if (index < filesName.size - 1) {
                                    delay(6000L)
                                }
                            }
                        } catch (e: IOException) {
                            Log.e(TAG, "Error:a ${e.message} ", e)
                            logger.error("Ошибка: ${e.message}")
                        } finally {
                            releaseInterface(printerInterface)
                            close()
                        }


                    } ?: Log.e(TAG, "Не удалось открыть устройство")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        saveState(isActive = false)
        Log.d(TAG, "onDestroy")

        timerJob?.cancel()
        serviceJob?.cancel()
        coroutineScope.coroutineContext.cancelChildren()

        unregisterUsbReceiver()

        Toast.makeText(this, "Foreground Service destroyed", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private val TIMER_PERIOD_MILISECOND = 120000L
        private const val ACTION_USB_PERMISSION =
            "com.vc.vcposprintservice.presentation.USB_PERMISSION"

        private const val TAG = "USB_HOST_API"
        private const val TIMEOUT = 0
    }
}