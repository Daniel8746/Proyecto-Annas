package com.pmdm.annas.download

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.CancellationException

const val DESKTOP_UA =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"

@SuppressLint("SetJavaScriptEnabled")
fun launchSilentDownload(
    context: Context,
    url: String,
    onDownloadStart: (String, String, String, String, Long, String?) -> Unit
) {
    // Es importante que el WebView se cree en el hilo principal
    WebView(context).apply {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            userAgentString = DESKTOP_UA
            // Permitir redirecciones y popups silenciosos
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(true)
        }
        
        clearCache(true)
        
        // CORRECCIÓN: Se debe asignar el webViewClient, no solo crear el objeto
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val js = """
                        (function() {
                            function bypassAndExtract() {
                                // 1. Resetear el timer si existe
                                const timerIds = ['wait-timer', 'timer', 'seconds'];
                                timerIds.forEach(id => {
                                    const el = document.getElementById(id);
                                    if (el) { el.innerText = '0'; el.textContent = '0'; }
                                });
                                if (typeof window.seconds_remaining !== 'undefined') window.seconds_remaining = 0;

                                // 2. Buscar el SPAN con la URL (según la estructura de Anna's Archive)
                                const urlSpans = document.querySelectorAll('span.break-all');
                                for (let span of urlSpans) {
                                    const text = span.innerText.trim();
                                    if (text.startsWith('http')) {
                                        window.location.href = text; // Forzamos la descarga navegando a la URL
                                        return true;
                                    }
                                }

                                // 3. Backup: Buscar el botón de "copy" que tiene la URL en el atributo onclick
                                const copyBtn = document.querySelector('button[onclick*="http"]');
                                if (copyBtn) {
                                    const attr = copyBtn.getAttribute('onclick');
                                    const match = attr.match(/https?:\/\/[^']+/);
                                    if (match) {
                                        window.location.href = match[0];
                                        return true;
                                    }
                                }

                                // 4. Intentar clicar botones normales si aparecieran
                                const btn = document.querySelector('a.download-button') || document.querySelector('.js-download-link');
                                if (btn && btn.offsetParent !== null) {
                                    btn.click();
                                    return true;
                                }
                                return false;
                            }

                            // Ejecutar periódicamente hasta encontrar el enlace o agotar tiempo
                            const interval = setInterval(() => { if (bypassAndExtract()) clearInterval(interval); }, 1000);
                            const observer = new MutationObserver(() => { if (bypassAndExtract()) { observer.disconnect(); clearInterval(interval); } });
                            observer.observe(document.body, { childList: true, subtree: true });
                            setTimeout(() => { clearInterval(interval); observer.disconnect(); }, 30000);
                        })();
                    """.trimIndent()
                view?.evaluateJavascript(js, null)
            }

            override fun shouldOverrideUrlLoading(
                v: WebView?,
                r: WebResourceRequest?
            ): Boolean {
                val rUrl = r?.url?.toString() ?: ""
                // Si detectamos que es un enlace directo de descarga, lo capturamos
                if (isDirect(rUrl)) {
                    onDownloadStart(rUrl, DESKTOP_UA, guessCD(rUrl), getMime(rUrl), 0, v?.url)
                    // No destruimos aquí para dejar que termine el proceso si es necesario, 
                    // o podemos destruirlo si ya tenemos lo que queremos.
                    return true
                }
                return false
            }
        }

        setDownloadListener { dUrl, ua, cd, mime, len ->
            // El DownloadListener es la forma más fiable de capturar la descarga real
            onDownloadStart(dUrl, ua, cd, mime, len, this.url)
            // Una vez capturado, ya no necesitamos este WebView
            post { destroy() }
        }
        
        loadUrl(url)
    }
}

fun guessCD(url: String): String {
    val fileName = url.substringAfterLast("/", "").substringBefore("?")
    return if (fileName.isNotEmpty() && fileName.contains(".")) {
        "attachment; filename=\"$fileName\""
    } else ""
}

fun getMime(url: String) = when {
    url.contains(".pdf", ignoreCase = true) -> "application/pdf"
    url.contains(".epub", ignoreCase = true) -> "application/epub+zip"
    url.contains(".mobi", ignoreCase = true) -> "application/x-mobipocket-ebook"
    url.contains(".azw3", ignoreCase = true) -> "application/x-mobipocket-ebook"
    url.contains(".zip", ignoreCase = true) -> "application/zip"
    else -> "application/octet-stream"
}

fun isDirect(url: String): Boolean {
    val low = url.lowercase()
    // Evitar capturar la página de espera de Anna's Archive como descarga
    if (low.contains("slow_download") && low.contains("annas-archive.org")) return false

    return listOf(".pdf", ".epub", ".mobi", ".azw3", ".zip").any {
        low.endsWith(it) || low.contains("$it?")
    } || low.contains("get.php") || low.contains("libgen") || low.contains("/get/") || (low.contains(
        "/download/"
    ) && !low.contains("annas-archive.org")) || low.contains(":6060")
}

suspend fun downloadFileWithNotification(
    context: Context, client: OkHttpClient, url: String, ua: String, cd: String?, mime: String?,
    dest: Uri, fileName: String, helper: NotificationHelper, len: Long = 0, ref: String? = null
) = coroutineScope {
    val job = coroutineContext[Job]
    val cJob = launch {
        try {
            DownloadEvents.cancelFlow.collect { job?.cancel(CancellationException()) }
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
                    if (attempt > 1) "($attempt/$maxAttempts) $fileName" else fileName,
                    0
                )
                val cookies = CookieManager.getInstance().getCookie(url)
                val req = Request.Builder()
                    .url(url)
                    .header("User-Agent", ua)
                    .header("Accept", mime ?: "*/*")
                    .apply {
                        cd?.let { if (it.isNotBlank()) header("Content-Disposition", it) }
                        ref?.let { header("Referer", it) }
                        cookies?.let { header("Cookie", it) }
                    }.build()

                client.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) throw IOException("Unexpected code $resp")

                    val body = resp.body
                    val total = if (body.contentLength() > 0) body.contentLength() else len
                    if (total <= 0) helper.showProgressNotification(fileName, -1)

                    var current = 0L
                    context.contentResolver.openOutputStream(dest)?.use { out ->
                        val buffer = ByteArray(64 * 1024)
                        var bytes: Int
                        var lastUpdate = 0L
                        body.byteStream().use { input ->
                            while (input.read(buffer).also { bytes = it } != -1) {
                                if (!isActive) throw CancellationException()
                                out.write(buffer, 0, bytes)
                                current += bytes
                                val now = System.currentTimeMillis()
                                if (total > 0 && now - lastUpdate > 800) {
                                    helper.showProgressNotification(
                                        if (attempt > 1) "($attempt/$maxAttempts) $fileName" else fileName,
                                        ((current * 100) / total).toInt()
                                    )
                                    lastUpdate = now
                                }
                            }
                        }
                    }

                    if (total > 0 && current < total) {
                        throw IOException("Incomplete download: received $current of $total bytes")
                    }

                    success = true
                    helper.showCompletedNotification(fileName, dest)
                }
            }
        } catch (e: Exception) {
            if (e is CancellationException) {
                helper.cancelNotification()
                break
            }

            // Borrar el archivo parcial antes del reintento
            try {
                DocumentsContract.deleteDocument(context.contentResolver, dest)
            } catch (_: Exception) {
            }

            if (attempt < maxAttempts) {
                delay(2000) // Esperar 2 segundos antes de reintentar
            } else {
                helper.showErrorNotification(fileName)
            }
        }
    }
    cJob.cancel()
}
