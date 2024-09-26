package com.vc.vcposprintservice.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.vc.vcposprintservice.R
import com.vc.vcposprintservice.presentation.main.MainActivity

internal object NotificationHelper {

    private const val NOTIFICATION_CHANNEL_ID = "general_notification_channel"
    const val NOTIFICATION_ID = 1

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.foreground_service_sample_notification_channel_general_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val notificationManager =
                context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildNotification(context: Context, contentText: String): Notification =
        NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.foreground_service_notification_title))
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_print_status_icon)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentIntent(Intent(context, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(
                    context,
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            })
            .build()

    fun updateNotification(context: Context, contentText: String) {
        val notification = buildNotification(context = context, contentText = contentText)
        val notificationManager =
            context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}