package com.pmdm.annas.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) {
    private val channelId = "downloads_channel"
    private val notificationId = 1001
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            "Descargas de libros",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Muestra el progreso de descarga de los libros"
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun showProgressNotification(fileName: String, progress: Int) {
        val cancelIntent = Intent(context, NotificationCancelReceiver::class.java).apply {
            action = "CANCEL_DOWNLOAD"
        }
        val cancelPendingIntent = PendingIntent.getBroadcast(
            context, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Descargando $fileName")
            .setContentText("$progress%")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setProgress(100, progress, false)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancelar", cancelPendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun showCompletedNotification(fileName: String, fileUri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, "application/octet-stream")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("¡Descarga completada!")
            .setContentText(fileName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun showErrorNotification(fileName: String) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("Error en la descarga")
            .setContentText("No se pudo descargar $fileName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
    
    fun cancelNotification() {
        notificationManager.cancel(notificationId)
    }
}
