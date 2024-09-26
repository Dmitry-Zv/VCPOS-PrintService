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
import java.io.FileInputStream
import java.io.IOException
import javax.inject.Inject


@AndroidEntryPoint
class PrintService : Service() {

    private val logger: Logger = LoggerFactory.getLogger(PrintService::class.java)

    @Inject
    lateinit var getFiles: GetFiles

    @Inject
    lateinit var getPrinter: GetPrinter

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
                    Log.d(TAG, "USB device attached")
                    logger.info("Usb устройство подключено")
                    serviceJob = CoroutineScope(Dispatchers.IO).launch {
                        checkUsbDeviceList()
                    }
                }

                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    Log.d(TAG, "USB device detached")
                    logger.info("USB устройство отключено")
                    if (usbDevice == intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)) {
                        Log.d(TAG, "Our printer was detached")
                        logger.info("Принтер был отключен")
                        stopPrinting()
                    }
                }

                else -> {
                    Log.d(TAG, "permission denied for device ${usbDevice.productName}")
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
//        manager.hasPermission(usbDevice)
//        startPrinting()
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
                            Log.e(TAG, result.exception.message, result.exception)
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
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@PrintService,
                            "Foreground Service still running!",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка во время выполнения задачи: ${e.message}")
                    logger.error("Ошибка во время выполнения задачи: ${e.message}")
                    NotificationHelper.updateNotification(
                        context = this@PrintService,
                        contentText = "Error: ${e.message}"
                    )
                }

                delay(TIMER_PERIOD_MILISECOND)

            }
        }
    }

    private fun stopPrinting() {
        timerJob?.cancel()
        Log.d(TAG, "Printing stopped due to device detachment")
    }

    private suspend fun checkUsbDeviceList() {
        manager = getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList = manager.deviceList
        when (val result = getPrinter()) {
            is Result.Error -> Log.e(TAG, result.exception.message, result.exception)
            is Result.Success -> {
                val printerDevice = result.data
                deviceList.values.forEach { device ->
                    Log.d("USB_SERVICE", "Найдено устройство: ${device.productName}")
                    if (
                        printerDevice.deviceId == device.deviceId &&
                        printerDevice.vendorId == device.vendorId &&
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
            Log.e(TAG, "Receiver was not registered: ${e.message}")
        }
    }

    private fun requestPermission() {
        val permissionIntent = PendingIntent.getBroadcast(
            this, 0,
            Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE
        )
        manager.requestPermission(usbDevice, permissionIntent)
    }

    private fun setUpCommunication(device: UsbDevice?, filesName: List<String>) {
        filesName.forEach { fileName ->
            NotificationHelper.updateNotification(
                context = this,
                contentText = "Печать файла $fileName"
            )
            val file = File(filesDir, fileName)
            if (!file.exists()) {
                logger.error("Файл с именем $fileName не существует")
                Log.e(TAG, "File with name: $fileName doesn't exist")
                return
            }
            var inputStream: FileInputStream? = null
            try {
                inputStream = openFileInput(fileName)
            } catch (e: IOException) {
                Log.e(TAG, "Failed to open file $fileName, ${e.message} ", e)
                logger.error("Ошибка открытия файла $fileName: ${e.message}")
            }
            if (device != null && manager.hasPermission(device)) {
                device.getInterface(0).also { printerInterface ->
                    printerInterface.getEndpoint(0).also { endpoint ->
                        manager.openDevice(device)?.apply {
                            claimInterface(printerInterface, forceClaim)
                            try {
                                val buffer = ByteArray(4096)
                                var bytesRead: Int
                                while (inputStream?.read(buffer).also {
                                        bytesRead = it ?: -1
                                    } != -1) {
                                    val result = bulkTransfer(endpoint, buffer, bytesRead, TIMEOUT)
                                    if (result >= 0) {
                                        Log.d(
                                            TAG,
                                            "Данные успешно отправлены: $result байт"
                                        )
                                    } else {
                                        Log.e(TAG, "Не удалось отправить данные")
                                        logger.error("Не удалось отправить данные для файла $fileName")
                                    }
                                }
                                logger.info("Файл $fileName успешно отправлен на печать")
                                val deleted = file.delete()
                                if (deleted) {
                                    logger.info("Файл $fileName удален")
                                    println("File deleted immediately.")
                                } else {
                                    logger.error("Ошибка удаления файла $fileName")
                                    println("Failed to delete file.")
                                }
                            } catch (e: IOException) {
                                Log.e(TAG, "Ошибка чтения файла: ${e.message}")
                                logger.error("Ошибка чтения файла $fileName: ${e.message}")
                            } finally {
                                inputStream?.close()
                                releaseInterface(printerInterface)
                                close()
                            }

                        } ?: Log.e(TAG, "Не удалось открыть устройство")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")

        timerJob?.cancel()
        serviceJob?.cancel()
        coroutineScope.coroutineContext.cancelChildren()

        unregisterUsbReceiver()

        Toast.makeText(this, "Foreground Service destroyed", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private val TIMER_PERIOD_MILISECOND = 30000L
        private const val ACTION_USB_PERMISSION =
            "com.vc.vcposprintservice.presentation.USB_PERMISSION"

        private const val TAG = "USB_HOST_API"
        private const val TIMEOUT = 0
    }
}