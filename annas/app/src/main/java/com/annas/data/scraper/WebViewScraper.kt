package com.annas.data.scraper

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.MainThread
import com.annas.data.js.JsEngine
import com.annas.data.js.JsScripts.DOM_HTML_COLLECTOR
import com.annas.data.js.JsScripts.HTML_CAPTURE_AND_SEND
import com.annas.data.utils.isUnnecessaryResource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import org.json.JSONTokener
import java.io.ByteArrayInputStream
import java.util.concurrent.CancellationException
import javax.inject.Inject
import kotlin.coroutines.resume

class WebViewScraper @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private val mutex = Mutex()

    private var currentCssSelector = ""

    @Volatile
    private var webView: WebView? = null

    @Volatile
    private var currentContinuation: CancellableContinuation<String>? = null

    @SuppressLint("SetJavaScriptEnabled")
    @MainThread
    private fun getOrCreateWebView(): WebView {

        webView?.let { return it }

        val wv = WebView(context).apply {

            settings.apply {

                javaScriptEnabled = true
                domStorageEnabled = true

                cacheMode = WebSettings.LOAD_DEFAULT

                loadsImagesAutomatically = false
                blockNetworkImage = true

                setSupportZoom(false)

                displayZoomControls = false

                useWideViewPort = false

                loadWithOverviewMode = false

                mediaPlaybackRequiresUserGesture = true
                javaScriptCanOpenWindowsAutomatically = false
                setSupportMultipleWindows(false)
                textZoom = 100
                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

                userAgentString =
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
            }

            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

            addJavascriptInterface(object {

                @Suppress("unused")
                @JavascriptInterface
                fun onHtmlReady(html: String) {

                    val continuation = currentContinuation

                    if (continuation?.isActive == true) {

                        continuation.resume(html)

                        currentContinuation = null
                    }
                }

            }, "Android")

            webViewClient = object : WebViewClient() {

                override fun shouldInterceptRequest(
                    view: WebView?, request: WebResourceRequest?
                ): WebResourceResponse? {

                    val requestUrl = request?.url?.toString()?.lowercase() ?: ""

                    if (isUnnecessaryResource(requestUrl)) {

                        return WebResourceResponse(
                            "text/plain", "UTF-8", ByteArrayInputStream(ByteArray(0))
                        )
                    }

                    return null
                }


                override fun onPageCommitVisible(
                    view: WebView?, url: String?
                ) {

                    injectCurrentScraperScript(view)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    injectCurrentScraperScript(view)
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: android.webkit.WebResourceError?
                ) {

                    if (request?.isForMainFrame == true) {

                        currentContinuation?.let {

                            if (it.isActive) it.resume("")
                        }

                        currentContinuation = null
                        currentCssSelector = ""
                    }
                }
            }
        }

        webView = wv

        return wv
    }


    private fun injectCurrentScraperScript(view: WebView?) {
        if (currentCssSelector.isNotEmpty()) {
            injectScraperScript(
                view, currentCssSelector
            )
        }
    }

    private fun injectScraperScript(
        view: WebView?, cssSelector: String
    ) {
        val selectorJs = JSONObject.quote(cssSelector)

        view?.evaluateJavascript(
            JsEngine.render(
                HTML_CAPTURE_AND_SEND,
                mapOf(
                    "SELECTOR" to selectorJs,
                    "TIMEOUT" to 8500
                )
            ), null
        )
    }


    suspend fun loadUrlAndGetHtml(
        url: String, cssSelector: String, timeoutMs: Long = 9000
    ): String = mutex.withLock {

        withContext(Dispatchers.Main) {

            val wv = getOrCreateWebView()

            try {

                val html = withTimeout(timeoutMs) {

                    suspendCancellableCoroutine { cont ->

                        currentContinuation = cont

                        currentCssSelector = cssSelector

                        wv.stopLoading()

                        wv.loadUrl(url)

                        cont.invokeOnCancellation {

                            wv.stopLoading()

                            if (currentContinuation === cont) {
                                currentContinuation = null
                            }
                        }
                    }
                }

                html.ifEmpty {
                    getInstantHtml(
                        wv, cssSelector
                    )
                }

            } catch (_: TimeoutCancellationException) {
                getInstantHtml(
                    wv, cssSelector
                )

            } catch (e: CancellationException) {

                throw e

            } finally {

                currentContinuation = null
                currentCssSelector = ""
            }
        }
    }

    private suspend fun getInstantHtml(
        wv: WebView, cssSelector: String
    ): String = suspendCancellableCoroutine { cont ->

        val selectorJs = JSONObject.quote(cssSelector)

        wv.evaluateJavascript(
            JsEngine.render(
                DOM_HTML_COLLECTOR,
                mapOf(
                    "SELECTOR" to selectorJs
                )
            )
        ) { html ->

            if (!cont.isActive) return@evaluateJavascript

            val clean = decodeJavascriptString(html)

            cont.resume(clean)
        }
    }

    private fun decodeJavascriptString(value: String?): String {
        if (value.isNullOrBlank() || value == "null") return ""

        return try {
            JSONTokener(value).nextValue() as? String ?: ""
        } catch (_: Exception) {
            value.removePrefix("\"")
                .removeSuffix("\"")
                .replace("\\u003C", "<")
                .replace("\\\"", "\"")
        }
    }


    suspend fun limpiarWebViewStorage() = withContext(Dispatchers.Main) {

        CookieManager.getInstance().removeAllCookies(null)

        CookieManager.getInstance().flush()

        WebStorage.getInstance().deleteAllData()

        webView?.apply {

            clearCache(true)

            clearHistory()

            clearFormData()
        }
    }
}
