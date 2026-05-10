package com.pmdm.annas.data.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationHelper @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val channelId = "downloads_channel"
    private val notificationId = 1001
    private val requestCode = 2001
    private val cancelCode = 3001
    private val manager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        manager.createNotificationChannel(
            NotificationChannel(
                channelId, "Descargas", NotificationManager.IMPORTANCE_HIGH
            )
        )
    }

    fun showProgressNotification(fileName: String, progress: Int, speedText: String = "") {
        val cancelPI = PendingIntent.getBroadcast(
            context, notificationId, Intent(context, NotificationCancelReceiver::class.java).apply {
                action = "CANCEL_DOWNLOAD"
            }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isprogress = progress in 0..100
        val statusText = when {
            isprogress -> "$progress%  •  $speedText"
            else -> "Descargando...  •  $speedText"
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download).setContentTitle(fileName)
            .setContentText(statusText)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancelar", cancelPI)

        if (isprogress) builder.setProgress(100, progress, false)
        else builder.setProgress(0, 0, true)

        manager.notify(notificationId, builder.build())
    }

    fun showCompletedNotification(
        fileName: String, uri: Uri, mime: String? = "application/octet-stream"
    ) {
        manager.cancel(notificationId)
        val pi = PendingIntent.getActivity(
            context, requestCode, Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mime)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        manager.notify(
            notificationId,
            NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("Completado")
                .setContentText(fileName).setAutoCancel(true).setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_HIGH).build()
        )
    }

    fun showErrorNotification(fileName: String) {
        manager.cancel(notificationId)
        manager.notify(
            cancelCode,
            NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.stat_notify_error).setContentTitle("Error")
                .setContentText(fileName).setAutoCancel(true).build()
        )
    }

    fun cancelNotification(fileName: String) {
        manager.cancel(notificationId)

        manager.notify(
            cancelCode,
            NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_menu_close_clear_cancel)
                .setContentTitle("Cancelar Descarga")
                .setContentText(fileName).setAutoCancel(true).build()
        )
    }
}