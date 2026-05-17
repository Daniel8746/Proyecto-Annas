package com.annas.data.network

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.annas.data.js.JsScripts
import com.annas.data.notifications.NotificationHelper
import com.annas.data.utils.isUnnecessaryResource
import com.annas.data.utils.safeDestroy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Buffer
import okio.buffer
import okio.sink
import javax.inject.Inject
import javax.inject.Named

const val DESKTOP_UA =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"

class SilentDownloader @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:Named("downloadClient") private val client: OkHttpClient
) {
    val cookie: CookieManager = CookieManager.getInstance().apply {
        setAcceptCookie(true)
    }

    var destroyScheduled = false

    lateinit var destroyRunnable: Runnable

    var onTiempoEspera: ((Int) -> Unit)? = null

    @SuppressLint("SetJavaScriptEnabled")
    fun launchSilentDownload(
        activity: Activity,
        url: String,
        onDownloadStart: (String, String, String, String, Long, String?) -> Unit
    ) {
        val wv = WebView(activity).apply {
            destroyScheduled = false

            cookie.setAcceptThirdPartyCookies(this, true)

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
                userAgentString = DESKTOP_UA
                javaScriptCanOpenWindowsAutomatically = true
                setSupportMultipleWindows(true)
                loadsImagesAutomatically = false
                blockNetworkImage = true
                setSupportZoom(false)
                builtInZoomControls = false
                displayZoomControls = false
                mediaPlaybackRequiresUserGesture = true

                allowFileAccess = false
                allowContentAccess = false
                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            }

            addJavascriptInterface(object {
                @Suppress("unused")
                @JavascriptInterface
                fun obtenerTiempoEspera(tiempoEspera: Int) {
                    onTiempoEspera?.invoke(tiempoEspera)
                }
            }, "Android")
        }

        destroyRunnable = Runnable {
            try {
                safeDestroy(wv, destroyRunnable)
            } catch (_: Exception) {
            }
        }

        wv.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?, request: WebResourceRequest?
            ): WebResourceResponse? {

                val requestUrl = request?.url?.toString()?.lowercase().orEmpty()

                return if (isUnnecessaryResource(requestUrl)) {
                    WebResourceResponse(
                        "text/plain", "UTF-8", Buffer().inputStream()
                    )
                } else null
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                injectAutoExtractor(view)

                if (!destroyScheduled) {
                    destroyScheduled = true
                    // Destruye el WebView de forma segura tras 35 segundos
                    view?.postDelayed(destroyRunnable, 35_000)
                }
            }

            override fun shouldOverrideUrlLoading(
                v: WebView?, r: WebResourceRequest?
            ): Boolean {
                val rUrl = r?.url?.toString().orEmpty()

                if (isDirect(rUrl)) {
                    onDownloadStart(
                        rUrl, DESKTOP_UA, guessCD(rUrl), getMime(rUrl), 0, v?.url
                    )

                    v?.postDelayed(destroyRunnable, 1000)
                    return true
                }

                return false
            }


            override fun onReceivedError(
                view: WebView?, request: WebResourceRequest?, error: WebResourceError?
            ) {
                if (request?.isForMainFrame == true) {
                    view?.postDelayed(destroyRunnable, 1000)
                }
            }
        }

        wv.setDownloadListener { dUrl, ua, cd, mime, len ->
            cookie.flush()

            onDownloadStart(
                dUrl, ua, cd, mime, len, wv.url
            )

            wv.postDelayed(destroyRunnable, 100)
        }

        wv.loadUrl(url)
    }

    suspend fun downloadFileWithNotification(
        url: String,
        ua: String,
        cd: String?,
        mime: String?,
        dest: Uri,
        fileName: String,
        helper: NotificationHelper,
        len: Long = 0,
        ref: String? = null
    ) = coroutineScope {
        val job = coroutineContext.job

        val cancelJob = launch {
            try {
                DownloadEvents.cancelFlow.collect {

                    job.cancel(
                        CancellationException()
                    )
                }

            } catch (_: Exception) {
            }
        }

        var attempt = 0
        val maxAttempts = 3
        var success = false

        while (attempt < maxAttempts && !success && isActive) {
            attempt++

            try {
                withContext(Dispatchers.IO) {
                    helper.showProgressNotification(
                        if (attempt > 1) "($attempt/$maxAttempts) $fileName"
                        else fileName, 0
                    )

                    val request = Request.Builder()
                        .url(url)
                        .header("User-Agent", ua)
                        .header(
                            "Accept",
                            "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8"
                        )
                        .header("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
                        .header("Accept-Encoding", "identity")
                        .header(
                            "Sec-Ch-Ua",
                            "\"Chromium\";v=\"122\", \"Not(A:Brand\";v=\"24\", \"Google Chrome\";v=\"122\""
                        )
                        .header("Sec-Ch-Ua-Mobile", "?0")
                        .header("Sec-Ch-Ua-Platform", "\"Windows\"")
                        .header("Sec-Fetch-Dest", "document")
                        .header("Sec-Fetch-Mode", "navigate")
                        .header("Sec-Fetch-Site", "none")
                        .header("Sec-Fetch-User", "?1")
                        .header("Upgrade-Insecure-Requests", "1")
                        .apply {
                            ref?.let { header("Referer", it) }

                            cookie.getCookie(url)?.let {
                                header("Cookie", it)
                            }

                            cd?.takeIf { it.isNotBlank() }?.let {
                                header("Content-Disposition", it)
                            }
                        }
                        .build()

                    client.newCall(request).execute().use { resp ->

                        if (!resp.isSuccessful) throw okio.IOException(
                            "Unexpected code $resp"
                        )

                        val body = resp.body
                        val total = body.contentLength().takeIf { it > 0 } ?: len

                        if (total <= 0) helper.showProgressNotification(
                            fileName, -1
                        )

                        var current = 0L

                        val outputStream = context.contentResolver.openOutputStream(dest)
                            ?: throw okio.IOException(
                                "No se pudo abrir el destino"
                            )

                        val source = body.source()
                        val sink = outputStream.sink().buffer()

                        try {
                            val buffer = Buffer()
                            val readSize = 1024 * 1024L

                            var lastProgressUpdate = 0L
                            var lastPercent = -1
                            var bytesInLastInterval = 0L
                            var lastSpeedUpdateTick = System.currentTimeMillis()
                            var speedText = ""

                            while (source.read(buffer, readSize) != -1L) {
                                if (!isActive) throw CancellationException()

                                val bytesRead = buffer.size
                                sink.write(buffer, bytesRead)
                                current += bytesRead
                                bytesInLastInterval += bytesRead

                                val currentTime = System.currentTimeMillis()
                                val timeDiff = currentTime - lastSpeedUpdateTick

                                if (timeDiff >= 1000L) {
                                    val speedInBytesPerSecond =
                                        (bytesInLastInterval * 1000) / timeDiff
                                    speedText = formatSpeed(speedInBytesPerSecond)

                                    bytesInLastInterval = 0L
                                    lastSpeedUpdateTick = currentTime
                                }

                                if (total > 0) {
                                    val progress = (current * 100 / total).toInt()

                                    if (
                                        progress != lastPercent ||
                                        currentTime - lastProgressUpdate > 1000L
                                    ) {
                                        helper.showProgressNotification(
                                            if (attempt > 1) "($attempt/$maxAttempts) $fileName" else fileName,
                                            progress,
                                            speedText
                                        )

                                        lastPercent = progress
                                        lastProgressUpdate = currentTime
                                    }
                                }
                            }
                            sink.flush()
                        } finally {
                            source.close()
                            sink.close()
                        }

                        if (total > 0 && current < total) {
                            throw okio.IOException(
                                "Incomplete download"
                            )
                        }

                        success = true

                        helper.showCompletedNotification(
                            fileName, dest, mime
                        )
                    }

                }

            } catch (e: Exception) {
                if (e is CancellationException) {
                    helper.cancelNotification(fileName)
                    break
                }

                try {
                    DocumentsContract.deleteDocument(
                        context.contentResolver, dest
                    )

                } catch (_: Exception) {
                }

                if (attempt < maxAttempts) {
                    delay(2000L * attempt)
                } else {
                    helper.showErrorNotification(
                        fileName
                    )
                }
            }
        }

        cancelJob.cancel()
    }

    @SuppressLint("DefaultLocale")
    private fun formatSpeed(bytesPerSecond: Long): String {
        return when {
            bytesPerSecond >= 1024 * 1024 -> String.format(
                "%.1f MB/s",
                bytesPerSecond / (1024.0 * 1024.0)
            )

            bytesPerSecond >= 1024 -> String.format("%d KB/s", bytesPerSecond / 1024)
            else -> "$bytesPerSecond B/s"
        }
    }

    private fun injectAutoExtractor(view: WebView?) {
        view?.evaluateJavascript(JsScripts.AUTO_EXTRACT_AND_REDIRECT_SCRIPT, null)
    }
}
