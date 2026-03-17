package com.pmdm.annas.data.scraper

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.MainThread
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class WebViewScraper(@param:ApplicationContext private val context: Context) {
    @SuppressLint("SetJavaScriptEnabled")
    private val webView = WebView(context).apply {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.userAgentString =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36"
        CookieManager.getInstance().setAcceptCookie(true)
    }

    @MainThread
    suspend fun loadUrlAndGetHtml(url: String, cssSelector: String): String =
        suspendCancellableCoroutine { cont ->

            // Interfaz para recibir el HTML desde JS
            class WebAppInterface {
                @JavascriptInterface
                fun onHtmlReady(html: String) {
                    cont.resume(html)
                }
            }

            webView.addJavascriptInterface(WebAppInterface(), "Android")

            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    // Inyectamos el MutationObserver
                    val js = """
                        (function() {
                            const targetSelector = '$cssSelector';
                            const observer = new MutationObserver((mutations, obs) => {
                                if (document.querySelectorAll(targetSelector).length > 0) {
                                    const html = Array.from(document.querySelectorAll(targetSelector))
                                                      .map(div => div.outerHTML)
                                                      .join('');
                                    Android.onHtmlReady(html);
                                    obs.disconnect();
                                }
                            });
                            observer.observe(document.body, { childList: true, subtree: true });
                        })();
                    """.trimIndent()
                    view?.evaluateJavascript(js, null)
                }
            }

            webView.loadUrl(url)
        }
}