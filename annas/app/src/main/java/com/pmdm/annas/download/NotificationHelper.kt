package com.pmdm.annas.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) {
    private val channelId = "downloads_channel"
    private val notificationId = 1001
    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        manager.createNotificationChannel(NotificationChannel(channelId, "Descargas", NotificationManager.IMPORTANCE_HIGH))
    }

    fun showProgressNotification(fileName: String, progress: Int) {
        val cancelPI = PendingIntent.getBroadcast(context, notificationId, Intent(context, NotificationCancelReceiver::class.java).apply { action = "CANCEL_DOWNLOAD" }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download).setContentTitle(fileName).setOngoing(true).setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancelar", cancelPI)
        if (progress in 0..100) builder.setProgress(100, progress, false).setContentText("$progress%")
        else builder.setProgress(0, 0, true).setContentText("Descargando...")
        manager.notify(notificationId, builder.build())
    }

    fun showCompletedNotification(fileName: String, uri: Uri) {
        manager.cancel(notificationId)
        val pi = PendingIntent.getActivity(context, 0, Intent(Intent.ACTION_VIEW).apply { setDataAndType(uri, "application/octet-stream"); flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        manager.notify(notificationId, NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done).setContentTitle("Completado").setContentText(fileName)
            .setAutoCancel(true).setContentIntent(pi).setPriority(NotificationCompat.PRIORITY_HIGH).build())
    }

    fun showErrorNotification(fileName: String) {
        manager.cancel(notificationId)
        manager.notify(notificationId + 1, NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_error).setContentTitle("Error").setContentText(fileName).setAutoCancel(true).build())
    }

    fun cancelNotification() = manager.cancel(notificationId)
}
