package com.pmdm.annas.data.scraper

import android.content.Context
import android.util.LruCache
import androidx.core.content.edit
import com.pmdm.annas.data.repositorys.toLibros
import com.pmdm.annas.model.Libro
import com.pmdm.annas.uri.UriUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class Scraper @Inject constructor(
    private val webViewScraper: WebViewScraper,
    @param:Named("scraperClient") private val okHttpClient: OkHttpClient,
    @param:ApplicationContext private val context: Context
) {

    private val prefs by lazy {
        context.getSharedPreferences("scraper_prefs", Context.MODE_PRIVATE)
    }

    private var mirrorUrls = mutableListOf<String>()
    private var activeMirrorIndex = 0
    private val urlMirror = "https://shadowlibraries.github.io/DirectDownloads/AnnasArchive/"

    private val searchCache = LruCache<String, List<Libro>>(50)
    private val detailsCache = LruCache<String, Pair<String, List<String>>>(100)

    private var activeBaseUrl: String
        get() = mirrorUrls.getOrNull(activeMirrorIndex) ?: ""
        set(value) {
            val index = mirrorUrls.indexOf(value)
            if (index != -1) activeMirrorIndex = index
            prefs.edit { putString("mirror", value) }
        }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            initializeMirrors()
        }
    }

    private suspend fun initializeMirrors() {
        repeat(3) { attempt ->
            try {
                webViewScraper.limpiarWebViewStorage()
                findMirrors()
                findBestMirror()
                if (mirrorUrls.isNotEmpty()) return
            } catch (_: Exception) {}
            delay(1000L * (attempt + 1))
        }

        // Si aún no hay mirrors válidos, forzamos recarga
        if (mirrorUrls.isEmpty()) {
            webViewScraper.limpiarWebViewStorage()
            findMirrors()
            findBestMirror()
        }
    }

    private suspend fun findMirrors() {
        mirrorUrls.clear()
        repeat(3) { attempt ->
            try {
                val cssSelector = "ul"
                val html = webViewScraper.loadUrlAndGetHtml(urlMirror, cssSelector)
                val doc = Jsoup.parse(html)
                doc.select("li").forEach { li ->
                    val href = li.selectFirst("a")?.attr("href")?.substringBefore("/?") ?: ""
                    if (href.isNotEmpty()) mirrorUrls.add(href)
                }
                if (mirrorUrls.isNotEmpty()) return
            } catch (_: Exception) {}
            delay(1000L * (attempt + 1))
        }
    }

    private suspend fun findBestMirror() = coroutineScope {
        if (mirrorUrls.isEmpty()) return@coroutineScope
        val jobs = mirrorUrls.map { url ->
            async {
                try {
                    val request = Request.Builder().url(url).head().build()
                    val response = okHttpClient.newCall(request).execute()
                    if (response.isSuccessful) url else null
                } catch (_: Exception) { null }
            }
        }

        val best = jobs.awaitAll().firstOrNull { it != null } ?: ""
        if (best.isNotEmpty()) activeBaseUrl = best
    }

    private suspend fun <T> tryWithMirrors(block: suspend (mirror: String) -> T): T? {
        for (i in mirrorUrls.indices) {
            activeMirrorIndex = i
            try {
                return block(activeBaseUrl)
            } catch (_: Exception) {}
        }

        // Si todos fallan, reiniciamos mirrors
        webViewScraper.limpiarWebViewStorage()
        prefs.edit { remove("mirror") }
        initializeMirrors()
        return null
    }

    suspend fun buscarLibro(
        nombreLibro: String,
        extensiones: List<String> = emptyList(),
        idioma: String? = null,
        pagina: Int = 1
    ): List<Libro> = withContext(Dispatchers.IO) {
        val query = nombreLibro.trim()
        if (query.isEmpty()) return@withContext emptyList()

        val cacheKey = "$query-${extensiones.sorted().joinToString(",")}-$idioma-$pagina"
        searchCache.get(cacheKey)?.let { return@withContext it }

        val cssSelector = "div.flex.pt-3.pb-3.border-b"

        val result = tryWithMirrors { mirror ->
            val url = buildString {
                append(mirror)
                append("/search?q=")
                append(UriUtils.encode(query))
                append("&page=")
                append(pagina)
                extensiones.forEach { append("&ext=").append(it) }
                idioma?.let { append("&lang=").append(it) }
            }

            val html = webViewScraper.loadUrlAndGetHtml(url, cssSelector)
            val libros = Jsoup.parseBodyFragment(html).body().children().toLibros()

            // Solo cachear si hay libros
            if (libros.isNotEmpty()) searchCache.put(cacheKey, libros)
            libros
        }

        result ?: emptyList()
    }

    suspend fun servidorDescarga(enlace: String): Pair<String, List<String>> =
        withContext(Dispatchers.IO) {

            detailsCache.get(enlace)?.let { return@withContext it }

            val cssSelector = "main, .js-md5-top-box-description, h3, ul"

            val result = tryWithMirrors { mirror ->
                val url = if (enlace.startsWith("/")) "$mirror$enlace" else enlace
                val html = webViewScraper.loadUrlAndGetHtml(url, cssSelector)
                val doc = Jsoup.parse(html)

                val enlaceDescarga = mutableListOf<String>()
                val descripcion = doc.select(".js-md5-top-box-description div.mb-1")
                    .firstOrNull()?.text()?.trim() ?: "Sin descripción"

                val slowHeader = doc.select("h3")
                    .find { it.text().contains("Slow downloads", ignoreCase = true) }

                val ulLista = slowHeader?.parent()?.select("ul.list-inside").orEmpty()
                    .plus(slowHeader?.nextElementSiblings()?.select("ul.list-inside").orEmpty())
                    .firstOrNull()

                ulLista?.select("li")?.forEach { li ->
                    val text = li.text().lowercase()
                    if (text.contains("partner server") && text.contains("no waitlist")) {
                        val href = li.selectFirst("a")?.attr("href") ?: ""
                        if (href.isNotEmpty()) {
                            val fullUrl = if (href.startsWith("/")) "$mirror$href" else href
                            if (!enlaceDescarga.contains(fullUrl)) enlaceDescarga.add(fullUrl)
                        }
                    }
                }

                val pair = Pair(descripcion, enlaceDescarga)
                if (enlaceDescarga.isNotEmpty()) detailsCache.put(enlace, pair)
                pair
            }

            result ?: Pair("Error al obtener detalles", emptyList())
        }
}