package com.vc.vcposprintservice

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@HiltAndroidApp
class MyApp : Application() {
    private val logger: Logger = LoggerFactory.getLogger("PrintService")

    override fun onCreate() {
        super.onCreate()
        logger.info(
            """
                
            Запуск приложения  
            
        """.trimIndent()
        )
    }
}