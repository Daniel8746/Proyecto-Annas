package com.pmdm.annas.data.utils

import android.webkit.WebView

fun safeDestroy(v: WebView?) {
    try {
        v?.apply {
            stopLoading()
            loadUrl("about:blank")
            clearHistory()
            removeAllViews()
            destroy()
        }
    } catch (_: Exception) {
    }
}