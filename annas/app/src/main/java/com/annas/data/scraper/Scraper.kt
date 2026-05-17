package com.annas.data.scraper

import android.content.Context
import androidx.core.content.edit
import com.annas.data.cache.MemoryCache
import com.annas.data.network.DESKTOP_UA
import com.annas.data.repositorys.toLibros
import com.annas.model.Libro
import com.annas.uri.UriUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

class Scraper @Inject constructor(
    private val webViewScraper: WebViewScraper,
    @param:Named("scraperClient") private val okHttpClient: OkHttpClient,
    @param:ApplicationContext private val context: Context,
    private val cache: MemoryCache
) {

    private val prefs by lazy {
        context.getSharedPreferences("scraper_prefs", Context.MODE_PRIVATE)
    }

    private val mirrorUrls = mutableListOf<String>()
    private var activeMirrorIndex = 0
    private val urlMirror = "https://shadowlibraries.github.io/DirectDownloads/AnnasArchive/"

    private var activeBaseUrl: String
        get() = normalizeMirrorUrl(
            mirrorUrls.getOrNull(activeMirrorIndex) ?: prefs.getString("mirror", "").orEmpty()
        )
        set(value) {
            val normalized = normalizeMirrorUrl(value)
            val index = mirrorUrls.indexOf(normalized)

            if (index >= 0) activeMirrorIndex = index

            prefs.edit { putString("mirror", normalized) }
        }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var initializationJob: Job? = null

    private companion object {
        const val SEARCH_SELECTOR = "div.flex.pt-3.pb-3.border-b"
        const val DETAILS_SELECTOR = "main, .js-md5-top-box-description"
        const val HTTP_TIMEOUT_MS = 5500L
        const val MIRROR_TIMEOUT_MS = 5500L
    }

    init {
        initializationJob = scope.launch {
            val savedMirrors = prefs.getStringSet("mirror_list", emptySet())

            if (!savedMirrors.isNullOrEmpty()) {
                mirrorUrls.addAll(
                    savedMirrors
                        .map(::normalizeMirrorUrl)
                        .filter { it.isNotEmpty() }
                        .distinct()
                )

                val lastMirror = normalizeMirrorUrl(prefs.getString("mirror", "").orEmpty())
                activeMirrorIndex = mirrorUrls.indexOf(lastMirror).takeIf { it >= 0 } ?: 0
            }

            if (mirrorUrls.isEmpty()) {
                initializeMirrors()
            } else {
                checkActiveMirror()
            }
        }
    }

    private suspend fun checkActiveMirror() {
        val current = activeBaseUrl

        if (current.isEmpty() || !isMirrorAlive(current)) {
            initializeMirrors()
        }
    }

    private fun isMirrorAlive(url: String): Boolean {
        val normalized = normalizeMirrorUrl(url)

        if (normalized.isEmpty()) return false

        val headCode = executeForStatus(
            Request.Builder()
                .url(normalized)
                .withBrowserHeaders()
                .head()
                .build()
        )

        if (isAliveStatus(headCode)) return true
        if (headCode != null && headCode != 405) return false

        val getCode = executeForStatus(
            Request.Builder()
                .url(normalized)
                .withBrowserHeaders()
                .get()
                .build()
        )

        return isAliveStatus(getCode)
    }

    private suspend fun initializeMirrors() {
        repeat(2) { attempt ->
            try {
                findMirrors()

                if (mirrorUrls.isNotEmpty()) {
                    findBestMirror()

                    if (activeBaseUrl.isNotEmpty()) return
                }

                webViewScraper.limpiarWebViewStorage()

                delay(if (mirrorUrls.isEmpty()) 500L * (attempt + 1) else 200L)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
            }
        }
    }

    private suspend fun findMirrors() {
        repeat(2) { attempt ->
            try {
                val cssSelector = "ul"
                val html = loadSelectedHtml(urlMirror, cssSelector)

                if (html.isEmpty()) throw Exception("HTML vacio")

                val doc = Jsoup.parse(html, urlMirror)

                val newMirrors = doc.select("a[href]")
                    .map { link -> link.attr("abs:href").ifEmpty { link.attr("href") } }
                    .map(::normalizeMirrorUrl)
                    .filter { it.contains("annas-archive") }
                    .distinct()

                if (newMirrors.isNotEmpty()) {
                    mirrorUrls.clear()
                    mirrorUrls.addAll(newMirrors)

                    prefs.edit {
                        putStringSet("mirror_list", newMirrors.toSet())
                    }

                    return
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
            }

            delay(if (mirrorUrls.isEmpty()) 1000L * (attempt + 1) else 200L)
        }
    }

    private suspend fun findBestMirror() = coroutineScope {
        if (mirrorUrls.isEmpty()) return@coroutineScope

        val current = normalizeMirrorUrl(prefs.getString("mirror", "").orEmpty())

        if (current.isNotEmpty() && isMirrorAlive(current)) {
            activeBaseUrl = current
            return@coroutineScope
        }

        val jobs = mirrorUrls.map { url ->
            async {
                if (isMirrorAlive(url)) url else null
            }
        }

        val best = jobs.awaitAll().firstOrNull { it != null }.orEmpty()

        if (best.isNotEmpty()) activeBaseUrl = best
    }

    private suspend fun <T> tryWithMirrors(
        block: suspend (mirror: String) -> T
    ): T? {
        initializationJob?.join()

        val active = activeBaseUrl

        if (active.isNotEmpty()) {
            try {
                return block(active)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
            }
        }

        for (mirror in mirrorUrls.distinct()) {
            if (mirror == active) continue

            try {
                val result = block(mirror)
                activeBaseUrl = mirror
                return result
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
            }
        }

        webViewScraper.limpiarWebViewStorage()
        initializeMirrors()

        val finalActive = activeBaseUrl

        if (finalActive.isNotEmpty()) {
            try {
                return block(finalActive)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
            }
        }

        return null
    }

    suspend fun buscarLibro(
        nombreLibro: String,
        extensiones: List<String> = emptyList(),
        idioma: String? = null,
        pagina: Int = 1
    ): List<Libro> = withContext(Dispatchers.IO) {
        val query = nombreLibro.trim().lowercase()

        if (query.isEmpty()) return@withContext emptyList()

        val cacheKey =
            "$query-${extensiones.sorted().joinToString(separator = ",")}-$idioma-$pagina"

        cache.getSearch(cacheKey)?.let {
            return@withContext it
        }

        val cssSelector = SEARCH_SELECTOR

        val result = tryWithMirrors { mirror ->
            val url = buildString {
                append(mirror)
                append("/search?q=")
                append(UriUtils.encode(query))
                append("&page=")
                append(pagina)

                extensiones.forEach {
                    append("&ext=").append(it)
                }

                idioma?.let {
                    append("&lang=").append(it)
                }
            }

            val html = loadSelectedHtml(url, cssSelector)

            if (html.isEmpty()) throw Exception("No se obtuvo contenido")

            val libros = Jsoup.parseBodyFragment(html).body().children().toLibros()

            if (libros.isNotEmpty()) {
                cache.putSearch(cacheKey, libros)
            }

            libros
        }

        result ?: emptyList()
    }

    suspend fun servidorDescarga(
        enlace: String
    ): Pair<String, List<String>> = withContext(Dispatchers.IO) {

        cache.getDetails(enlace)?.let {
            return@withContext it
        }

        val cssSelector = DETAILS_SELECTOR

        val result = tryWithMirrors { mirror ->
            val url = resolveAgainstMirror(mirror, enlace)

            val html = loadSelectedHtml(url, cssSelector)

            if (html.isEmpty()) throw Exception("No se obtuvo contenido")

            val doc = Jsoup.parse(html, url)

            val enlaces = mutableListOf<String>()

            val descripcion =
                doc.select(".js-md5-top-box-description div.mb-1, .js-md5-top-box-description")
                    .firstOrNull { it.text().isNotBlank() }
                    ?.text()
                    ?.trim()
                    ?: "Sin descripcion"

            val slowHeader = doc.select("h3").find {
                it.text().contains(
                    "Slow downloads", ignoreCase = true
                )
            }

            slowHeader?.parent()?.select("a[href]")?.forEach { link ->
                val href = link.attr("href").trim()

                if (href.isEmpty() || href.startsWith("#")) return@forEach

                val label = "${link.text()} ${link.parent()?.text().orEmpty()} $href".lowercase()

                if (label.contains("partner server") || href.contains("slow_download")) {
                    val fullUrl = resolveAgainstMirror(mirror, href)

                    if (fullUrl.startsWith("http") && !enlaces.contains(fullUrl)) {
                        enlaces.add(fullUrl)
                    }
                }
            }

            val pair = Pair(descripcion, enlaces)

            if (enlaces.isNotEmpty()) {
                cache.putDetails(enlace, pair)
            }

            pair
        }

        result ?: Pair(
            "Error al obtener detalles", emptyList()
        )
    }

    private suspend fun loadSelectedHtml(
        url: String,
        cssSelector: String
    ): String {
        val directHtml = fetchSelectedHtml(url, cssSelector)

        if (directHtml.isNotEmpty()) return directHtml

        return webViewScraper.loadUrlAndGetHtml(url, cssSelector)
    }

    private fun fetchSelectedHtml(url: String, cssSelector: String): String {
        return try {
            val request = Request.Builder()
                .url(url)
                .withBrowserHeaders()
                .get()
                .build()

            val call = okHttpClient.newCall(request)
            call.timeout().timeout(HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS)

            call.execute().use { response ->
                if (!response.isSuccessful) return ""

                val doc = Jsoup.parse(response.body.string(), url)
                val selected = doc.select(cssSelector)

                selected.joinToString(separator = "") { it.outerHtml() }
            }
        } catch (_: Exception) {
            ""
        }
    }

    private fun executeForStatus(request: Request): Int? {
        return try {
            val call = okHttpClient.newCall(request)
            call.timeout().timeout(MIRROR_TIMEOUT_MS, TimeUnit.MILLISECONDS)

            call.execute().use { response ->
                response.code
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun isAliveStatus(code: Int?): Boolean {
        return code != null && (code in 200..399 || code == 403 || code == 429)
    }

    private fun Request.Builder.withBrowserHeaders(): Request.Builder = apply {
        header("User-Agent", DESKTOP_UA)
        header(
            "Accept",
            "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
        )
        header("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
    }

    private fun normalizeMirrorUrl(url: String): String {
        return url
            .trim()
            .substringBefore("/?")
            .substringBefore("?")
            .trimEnd('/')
    }

    private fun resolveAgainstMirror(mirror: String, href: String): String {
        val cleanHref = href.trim()
        val cleanMirror = normalizeMirrorUrl(mirror)

        return when {
            cleanHref.startsWith("http://") || cleanHref.startsWith("https://") -> cleanHref
            cleanHref.startsWith("//") -> "https:$cleanHref"
            cleanHref.startsWith("/") -> "$cleanMirror$cleanHref"
            else -> "$cleanMirror/$cleanHref"
        }
    }
}
