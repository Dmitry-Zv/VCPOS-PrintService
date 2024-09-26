package com.vc.vcposprintservice.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.vc.vcposprintservice.domain.usecases.auth.AuthUseCases
import com.vc.vcposprintservice.presentation.PrintService
import com.vc.vcposprintservice.utils.Result
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

@AndroidEntryPoint
class BootBroadcastReceiver : BroadcastReceiver() {

    private val logger: Logger = LoggerFactory.getLogger(BroadcastReceiver::class.java)

    @Inject
    lateinit var authUseCases: AuthUseCases

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED || intent?.action == Intent.ACTION_REBOOT) {
            Log.d("BootReceiver", "System has started, checking for data...")
            logger.info("Система была запущена....")

            CoroutineScope(Dispatchers.IO).launch {
                when (val result = authUseCases.getAuth()) {
                    is Result.Error -> {
                        Log.d(
                            "BootReceiver",
                            result.exception.message ?: "Unknown error"
                        )
                        logger.error(result.exception.message ?: "Неизвестная ошибка")
                    }

                    is Result.Success -> {
                        Log.d("BootReceiver", "Data found, starting ForegroundService...")
                        logger.info("Данные найдены. Запуск PrintService сервиса...")
                        val serviceIntent = Intent(context, PrintService::class.java).apply {
                            action = PrintService.Action.START.toString()
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context?.startForegroundService(serviceIntent)
                        } else {
                            context?.startService(serviceIntent)
                        }
                    }
                }
            }
        }
    }
}