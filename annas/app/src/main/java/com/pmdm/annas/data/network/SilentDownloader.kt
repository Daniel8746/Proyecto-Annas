package com.pmdm.annas.data.network

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.pmdm.annas.data.js.JsScripts
import com.pmdm.annas.data.notifications.NotificationHelper
import com.pmdm.annas.data.utils.isUnnecessaryResource
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
import java.io.ByteArrayInputStream
import java.io.IOException
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

    @SuppressLint("SetJavaScriptEnabled")
    fun launchSilentDownload(
        activity: Activity,
        url: String,
        onDownloadStart: (String, String, String, String, Long, String?) -> Unit
    ) {
        val wv = WebView(activity)

        cookie.setAcceptThirdPartyCookies(wv, true)

        wv.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            userAgentString = DESKTOP_UA
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(true)

            allowFileAccess = false
            allowContentAccess = false
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        }

        wv.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?, request: WebResourceRequest?
            ): WebResourceResponse? {

                val requestUrl = request?.url?.toString()?.lowercase().orEmpty()

                return if (isUnnecessaryResource(requestUrl)) {
                    WebResourceResponse(
                        "text/plain", "UTF-8", ByteArrayInputStream(ByteArray(0))
                    )
                } else null
            }

            override fun onPageFinished(
                view: WebView?, url: String?
            ) {
                view?.let { wv ->
                    wv.evaluateJavascript(JsScripts.AUTO_EXTRACT_AND_REDIRECT_SCRIPT, null)

                    // Destruye el WebView de forma segura tras 35 segundos
                    wv.postDelayed({
                        try {
                            safeDestroy(wv)
                        } catch (_: Exception) {
                            // evitar crashes si ya se destruyó
                        }
                    }, 35000)
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

                    safeDestroy(v)
                    return true
                }

                return false
            }


            override fun onReceivedError(
                view: WebView?, request: WebResourceRequest?, error: WebResourceError?
            ) {
                if (request?.isForMainFrame == true) {
                    safeDestroy(view)
                }
            }
        }

        wv.setDownloadListener { dUrl, ua, cd, mime, len ->
            onDownloadStart(
                dUrl, ua, cd, mime, len, wv.url
            )

            safeDestroy(wv)
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

                    val request = Request.Builder().url(url).header("User-Agent", ua)
                        .header("Accept", mime ?: "*/*").apply {
                            cd?.takeIf { it.isNotBlank() }
                                ?.let { header("Content-Disposition", it) }
                            ref?.let { header("Referer", it) }
                            CookieManager.getInstance().getCookie(url)?.let { header("Cookie", it) }
                        }.build()

                    client.newCall(request).execute().use { resp ->

                        if (!resp.isSuccessful) throw IOException(
                            "Unexpected code $resp"
                        )

                        val body = resp.body
                        val total = body.contentLength().takeIf { it > 0 } ?: len

                        if (total <= 0) helper.showProgressNotification(
                            fileName, -1
                        )

                        var current = 0L

                        val outputStream =
                            context.contentResolver.openOutputStream(dest) ?: throw IOException(
                                "No se pudo abrir el destino"
                            )

                        outputStream.use { out ->

                            val buffer = ByteArray(256 * 1024)

                            var bytes: Int

                            var lastUpdate = 0L


                            body.byteStream().use { input ->

                                while (input.read(buffer).also {
                                        bytes = it
                                    } != -1) {

                                    if (!isActive) throw CancellationException()

                                    out.write(
                                        buffer, 0, bytes
                                    )

                                    current += bytes

                                    val now = System.currentTimeMillis()

                                    if (total > 0 && now - lastUpdate > 1200) {

                                        helper.showProgressNotification(
                                            if (attempt > 1) "($attempt/$maxAttempts) $fileName"
                                            else fileName, (current * 100 / total).toInt()
                                        )

                                        lastUpdate = now
                                    }
                                }
                            }
                        }


                        if (total > 0 && current < total) {

                            throw IOException(
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

    private fun safeDestroy(v: WebView?) {
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
}