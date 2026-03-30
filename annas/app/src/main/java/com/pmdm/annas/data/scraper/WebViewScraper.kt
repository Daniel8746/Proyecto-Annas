package com.pmdm.annas.data.scraper

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
import com.pmdm.annas.utils.isUnnecessaryResource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.ByteArrayInputStream
import kotlin.coroutines.resume

class WebViewScraper(@param:ApplicationContext private val context: Context) {

    private val mutex = Mutex()
    private var currentCssSelector: String = ""

    @SuppressLint("SetJavaScriptEnabled")
    private val webView = WebView(context).apply {
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
            mediaPlaybackRequiresUserGesture = false
            userAgentString =
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36"
        }
        CookieManager.getInstance().setAcceptCookie(true)
    }

    private var currentContinuation: kotlinx.coroutines.CancellableContinuation<String>? = null

    init {
        setupWebView()
        setupCliente()
    }

    private fun setupWebView() {
        webView.addJavascriptInterface(object {
            @JavascriptInterface
            @Suppress("UNUSED")
            fun onHtmlReady(html: String) {
                currentContinuation?.let { if (it.isActive) it.resume(html) }
            }
        }, "Android")
    }

    @MainThread
    suspend fun loadUrlAndGetHtml(
        url: String,
        cssSelector: String,
        timeoutMs: Long = 8000
    ): String = mutex.withLock {
        withContext(Dispatchers.Main) {
            try {
                withTimeout(timeoutMs) {
                    suspendCancellableCoroutine { cont ->
                        currentContinuation = cont
                        currentCssSelector = cssSelector
                        webView.loadUrl(url)
                        cont.invokeOnCancellation {
                            webView.stopLoading()
                            currentContinuation = null
                        }
                    }
                }
            } catch (_: TimeoutCancellationException) {
                limpiarWebViewStorage()
                getInstantHtml(cssSelector)
            } finally {
                currentContinuation = null
            }
        }
    }

    private fun injectScraperScript(cssSelector: String) {
        val js = """
            (function() {
                const selector = '$cssSelector';
                function send() {
                    const el = document.querySelectorAll(selector);
                    if (el.length > 0) {
                        Android.onHtmlReady(Array.from(el).map(d => d.outerHTML).join(''));
                        return true;
                    }
                    return false;
                }
                if (!send()) {
                    const observer = new MutationObserver(() => { if (send()) observer.disconnect(); });
                    observer.observe(document.body, { childList: true, subtree: true });
                    setTimeout(() => { send(); observer.disconnect(); }, 5000);
                }
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    private suspend fun getInstantHtml(cssSelector: String): String =
        suspendCancellableCoroutine { cont ->
            webView.evaluateJavascript("(function() { return Array.from(document.querySelectorAll('$cssSelector')).map(d => d.outerHTML).join(''); })();") { html ->
                val cleanHtml = html?.removePrefix("\"")?.removeSuffix("\"")
                    ?.replace("\\u003C", "<")?.replace("\\\"", "\"") ?: ""
                cont.resume(cleanHtml)
            }
        }

    fun limpiarWebViewStorage() {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        WebStorage.getInstance().deleteAllData()
        webView.clearCache(true)
        webView.clearHistory()
        webView.clearFormData()
    }

    private fun setupCliente() {
        webView.webViewClient = object : WebViewClient() {

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {

                val requestUrl = request?.url?.toString()?.lowercase() ?: ""

                if (isUnnecessaryResource(requestUrl)) {
                    return WebResourceResponse(
                        "text/plain",
                        "UTF-8",
                        ByteArrayInputStream("".toByteArray())
                    )
                }

                return null
            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {
                if (currentCssSelector.isNotEmpty()) {
                    injectScraperScript(currentCssSelector)
                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: android.webkit.WebResourceError?
            ) {
                view?.destroy()
            }
        }
    }
}
