package com.pmdm.annas.data.scraper

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.MainThread
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

    @SuppressLint("SetJavaScriptEnabled")
    private val webView = WebView(context).apply {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            // Optimizaciones de velocidad
            loadsImagesAutomatically = false
            blockNetworkImage = true
            setSupportZoom(false)
            displayZoomControls = false
            userAgentString =
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36"
        }
        CookieManager.getInstance().setAcceptCookie(true)
    }

    private var currentContinuation: kotlinx.coroutines.CancellableContinuation<String>? = null

    init {
        setupWebView()
    }

    private fun setupWebView() {
        class WebAppInterface {
            @JavascriptInterface
            @Suppress("UNUSED")
            fun onHtmlReady(html: String) {
                currentContinuation?.let {
                    if (it.isActive) it.resume(html)
                }
            }
        }
        webView.addJavascriptInterface(WebAppInterface(), "Android")
    }

    @MainThread
    suspend fun loadUrlAndGetHtml(
        url: String,
        cssSelector: String,
        timeoutMs: Long = 15000
    ): String = mutex.withLock {
        withContext(Dispatchers.Main) {
            try {
                withTimeout(timeoutMs) {
                    suspendCancellableCoroutine { cont ->
                        currentContinuation = cont

                        webView.webViewClient = object : WebViewClient() {
                            override fun shouldInterceptRequest(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): WebResourceResponse? {
                                val requestUrl = request?.url?.toString() ?: ""
                                // Bloquear recursos innecesarios (imágenes, CSS, fuentes, analytics)
                                if (isUnnecessaryResource(requestUrl)) {
                                    return WebResourceResponse(
                                        "text/plain",
                                        "UTF-8",
                                        ByteArrayInputStream("".toByteArray())
                                    )
                                }
                                return super.shouldInterceptRequest(view, request)
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                injectScraperScript(cssSelector)
                            }
                        }

                        webView.loadUrl(url)

                        cont.invokeOnCancellation {
                            webView.stopLoading()
                            currentContinuation = null
                        }
                    }
                }
            } catch (_: TimeoutCancellationException) {
                // Si hay timeout, intentamos sacar lo que haya en el DOM en ese momento
                getInstantHtml(cssSelector)
            } finally {
                currentContinuation = null
            }
        }
    }

    private fun isUnnecessaryResource(url: String): Boolean {
        val lowerUrl = url.lowercase()
        return lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".png") || lowerUrl.endsWith(".jpeg") ||
                lowerUrl.endsWith(".gif") || lowerUrl.endsWith(".svg") || lowerUrl.endsWith(".css") ||
                lowerUrl.endsWith(".woff") || lowerUrl.endsWith(".woff2") || lowerUrl.endsWith(".ttf") ||
                lowerUrl.contains("google-analytics") || lowerUrl.contains("doubleclick") ||
                lowerUrl.contains("facebook") || lowerUrl.contains("amazon-adsystem")
    }

    private fun injectScraperScript(cssSelector: String) {
        val js = """
            (function() {
                const targetSelector = '$cssSelector';
                
                function sendHtml() {
                    const elements = document.querySelectorAll(targetSelector);
                    if (elements.length > 0) {
                        const html = Array.from(elements)
                                          .map(div => div.outerHTML)
                                          .join('');
                        Android.onHtmlReady(html);
                        return true;
                    }
                    return false;
                }

                if (!sendHtml()) {
                    const observer = new MutationObserver((mutations, obs) => {
                        if (sendHtml()) {
                            obs.disconnect();
                        }
                    });
                    observer.observe(document.body, { childList: true, subtree: true });
                    
                    // Fallback: si después de 5 segundos no hay cambios, enviar lo que haya
                    setTimeout(() => {
                        sendHtml();
                        observer.disconnect();
                    }, 5000);
                }
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    private suspend fun getInstantHtml(cssSelector: String): String =
        suspendCancellableCoroutine { cont ->
            val js = """
            (function() {
                const elements = document.querySelectorAll('$cssSelector');
                return Array.from(elements).map(div => div.outerHTML).join('');
            })();
        """.trimIndent()

            webView.evaluateJavascript(js) { html ->
                // evaluateJavascript devuelve el resultado como string JSON (con comillas)
                val cleanHtml =
                    html?.removePrefix("\"")?.removeSuffix("\"")?.replace("\\u003C", "<")
                        ?.replace("\\\"", "\"") ?: ""
                cont.resume(cleanHtml)
            }
        }
}
