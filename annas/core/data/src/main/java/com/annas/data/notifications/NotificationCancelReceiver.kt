package com.annas.data.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.annas.data.download.DownloadEvents

class NotificationCancelReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "CANCEL_DOWNLOAD") {
            DownloadEvents.cancelDownload()
        }
    }
}