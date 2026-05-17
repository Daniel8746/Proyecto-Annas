package com.annas.data.utils

import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

private var destroyed = false

fun safeDestroy(v: WebView?, r: Runnable) {
    if (destroyed) return

    destroyed = true

    try {
        v?.apply {
            removeCallbacks(r)
            stopLoading()
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            setDownloadListener(null)
            clearHistory()
            clearCache(false)
            loadUrl("about:blank")
            onPause()
            removeAllViews()
            destroy()
        }
    } catch (_: Exception) {
    }
}