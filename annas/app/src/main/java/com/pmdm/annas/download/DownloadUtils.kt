package com.pmdm.annas.download

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.CancellationException
import androidx.core.net.toUri

const val DESKTOP_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DownloadWebView(url: String, onDownloadStart: (String, String, String, String, Long) -> Unit) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.userAgentString = DESKTOP_USER_AGENT

                webViewClient = object : WebViewClient() {
                    @SuppressLint("UseKtx")
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val requestUrl = request?.url?.toString() ?: ""
                        
                        if (requestUrl.contains("docs.google.com/viewer")) {
                            val uri = requestUrl.toUri()
                            val realUrl = uri.getQueryParameter("url")
                            if (realUrl != null) {
                                onDownloadStart(realUrl, DESKTOP_USER_AGENT, guessContentDisposition(realUrl), getMimeTypeFromUrl(realUrl), 0)
                                return true
                            }
                        }

                        if (isDirectLink(requestUrl)) {
                            onDownloadStart(requestUrl, DESKTOP_USER_AGENT, guessContentDisposition(requestUrl), getMimeTypeFromUrl(requestUrl), 0)
                            return true
                        }
                        return false
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        // Inyectar script para buscar el link real y saltarse esperas
                        val js = """
                            (function() {
                                function findRealLink() {
                                    // 1. Buscar en botones de 'Copy' (común en Anna's Archive)
                                    const copyBtns = document.querySelectorAll('button[onclick*="navigator.clipboard"]');
                                    for (const btn of copyBtns) {
                                        const match = btn.getAttribute('onclick').match(/'(https?:\/\/[^']+)'/);
                                        if (match && match[1]) return match[1];
                                    }
                                    
                                    // 2. Buscar enlaces directos a archivos en la página
                                    const links = document.querySelectorAll('a');
                                    for (const a of links) {
                                        const h = a.href.toLowerCase();
                                        if (h.includes('.epub') || h.includes('.pdf') || h.includes('.mobi')) {
                                            if (!h.includes('annas-archive') || h.includes('/get/')) return a.href;
                                        }
                                    }
                                    
                                    // 3. Buscar texto que parezca una URL de descarga
                                    const spans = document.querySelectorAll('span');
                                    for (const s of spans) {
                                        const t = s.innerText.trim();
                                        if (t.startsWith('http') && (t.includes('.epub') || t.includes('.pdf'))) return t;
                                    }
                                    return null;
                                }
                                
                                const link = findRealLink();
                                if (link) { window.location.href = link; }
                            })();
                        """.trimIndent()
                        view?.evaluateJavascript(js, null)
                    }
                }

                setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                    onDownloadStart(url, userAgent, contentDisposition, mimetype, contentLength)
                }

                loadUrl(url)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

fun guessContentDisposition(url: String): String {
    val filename = url.substringAfterLast("/", "file").substringBefore("?")
    return "attachment; filename=\"$filename\""
}

fun getMimeTypeFromUrl(url: String): String {
    val lowerUrl = url.lowercase()
    return when {
        lowerUrl.contains(".pdf") -> "application/pdf"
        lowerUrl.contains(".epub") -> "application/epub+zip"
        lowerUrl.contains(".mobi") -> "application/x-mobipocket-ebook"
        lowerUrl.contains(".azw3") -> "application/vnd.amazon.ebook"
        lowerUrl.contains(".zip") -> "application/zip"
        lowerUrl.contains(".rar") -> "application/x-rar-compressed"
        else -> "application/octet-stream"
    }
}

fun isDirectLink(url: String): Boolean {
    val extensions = listOf(".pdf", ".epub", ".mobi", ".azw3", ".cbz", ".cbr", ".zip", ".rar")
    val lowerUrl = url.lowercase()
    
    // Si la URL contiene una extensión de libro
    val hasBookExtension = extensions.any { lowerUrl.contains(it) }
    
    // Ignorar si es una página de navegación conocida, a menos que sea un link IPFS/Libgen directo
    val isNavPage = lowerUrl.contains("/md5/") || lowerUrl.contains("/search?") || (lowerUrl.contains("slow_download") && !lowerUrl.contains(".epub") && !lowerUrl.contains(".pdf"))
    
    return (hasBookExtension && !isNavPage) ||
            lowerUrl.contains("get.php") ||
            lowerUrl.contains("libgen") ||
            lowerUrl.contains("ipfs") ||
            lowerUrl.contains("b4mcx2ml.net") // Servidor común de descargas directas
}

suspend fun downloadFileWithNotification(
    context: Context,
    client: OkHttpClient,
    url: String,
    userAgent: String,
    contentDisposition: String?,
    mimetype: String?,
    destinationUri: Uri,
    fileName: String,
    notificationHelper: NotificationHelper
) {
    coroutineScope {
        val cancelJob = launch {
            DownloadEvents.cancelFlow.first()
            this@coroutineScope.cancel("Descarga cancelada")
        }

        withContext(Dispatchers.IO) {
            try {
                notificationHelper.showProgressNotification(fileName, 0)

                val cookies = CookieManager.getInstance().getCookie(url)

                val requestBuilder = Request.Builder()
                    .url(url)
                    .header("User-Agent", userAgent)
                
                if (!mimetype.isNullOrEmpty()) {
                    requestBuilder.header("Accept", mimetype)
                }
                
                if (!contentDisposition.isNullOrEmpty()) {
                    requestBuilder.header("Content-Disposition", contentDisposition)
                }

                if (!cookies.isNullOrEmpty()) {
                    requestBuilder.header("Cookie", cookies)
                }

                val response = client.newCall(requestBuilder.build()).execute()

                response.use { resp ->
                    if (!resp.isSuccessful) {
                        notificationHelper.showErrorNotification(fileName)
                        return@withContext
                    }

                    val body = resp.body
                    val totalBytes = body.contentLength()

                    context.contentResolver.openOutputStream(destinationUri)?.use { output ->
                        val input = body.byteStream()
                        val buffer = ByteArray(8 * 1024)
                        var bytes: Int
                        var downloaded = 0L

                        while (input.read(buffer).also { bytes = it } != -1) {
                            if (!isActive) throw CancellationException()

                            output.write(buffer, 0, bytes)
                            downloaded += bytes

                            if (totalBytes > 0) {
                                val progress = ((downloaded * 100) / totalBytes).toInt()
                                notificationHelper.showProgressNotification(fileName, progress)
                            }
                        }
                    }
                    cancelJob.cancel()
                    notificationHelper.showCompletedNotification(fileName, destinationUri)
                }
            } catch (e: Exception) {
                cancelJob.cancel()
                if (e is CancellationException) {
                    notificationHelper.cancelNotification()
                } else {
                    notificationHelper.showErrorNotification(fileName)
                }
            }
        }
    }
}
