package com.pmdm.annas.download

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationCancelReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "CANCEL_DOWNLOAD") {
            DownloadEvents.cancelDownload()
        }
    }
}