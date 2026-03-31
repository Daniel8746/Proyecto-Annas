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

    private val searchCache = LruCache<String, List<Libro>>(100)
    private val detailsCache = LruCache<String, Pair<String, List<String>>>(200)

    private var activeBaseUrl: String
        get() = mirrorUrls.getOrNull(activeMirrorIndex) ?: prefs.getString("mirror", "") ?: ""
        set(value) {
            val index = mirrorUrls.indexOf(value)
            if (index != -1) activeMirrorIndex = index
            prefs.edit { putString("mirror", value) }
        }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var initializationJob: Job? = null

    init {
        // Cargar mirrors guardados si existen para tener algo inmediato
        val savedMirrors = prefs.getStringSet("mirror_list", emptySet())
        if (!savedMirrors.isNullOrEmpty()) {
            mirrorUrls.addAll(savedMirrors)
            val lastMirror = prefs.getString("mirror", "") ?: ""
            activeMirrorIndex = mirrorUrls.indexOf(lastMirror).coerceAtLeast(0)
        }

        initializationJob = scope.launch {
            if (mirrorUrls.isEmpty()) {
                initializeMirrors()
            } else {
                // Si ya tenemos mirrors, solo comprobamos si el activo sigue vivo
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

    private suspend fun isMirrorAlive(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).head().build()
            val response = okHttpClient.newCall(request).execute()
            response.isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun initializeMirrors() {
        repeat(2) { attempt ->
            try {
                // Intentamos buscar sin limpiar caché primero para ser más rápidos
                findMirrors()
                if (mirrorUrls.isNotEmpty()) {
                    findBestMirror()
                    if (activeBaseUrl.isNotEmpty()) return
                }
                
                // Si falla, entonces sí limpiamos para forzar recarga limpia del WebView
                webViewScraper.limpiarWebViewStorage()
                delay(500L * (attempt + 1))
            } catch (_: Exception) {}
        }
    }

    private suspend fun findMirrors() {
        repeat(2) { attempt ->
            try {
                val cssSelector = "ul"
                val html = webViewScraper.loadUrlAndGetHtml(urlMirror, cssSelector)
                if (html.isEmpty()) throw Exception("HTML vacío")
                
                val doc = Jsoup.parse(html)
                val newMirrors = mutableListOf<String>()
                doc.select("li").forEach { li ->
                    val href = li.selectFirst("a")?.attr("href")?.substringBefore("/?") ?: ""
                    if (href.isNotEmpty() && href.startsWith("http")) {
                        newMirrors.add(href)
                    }
                }
                
                if (newMirrors.isNotEmpty()) {
                    mirrorUrls.clear()
                    mirrorUrls.addAll(newMirrors)
                    prefs.edit { putStringSet("mirror_list", newMirrors.toSet()) }
                    return
                }
            } catch (_: Exception) {}
            delay(1000L * (attempt + 1))
        }
    }

    private suspend fun findBestMirror() = coroutineScope {
        if (mirrorUrls.isEmpty()) return@coroutineScope
        
        // Probamos el actual primero si existe
        val current = prefs.getString("mirror", "") ?: ""
        if (current.isNotEmpty() && isMirrorAlive(current)) {
            activeBaseUrl = current
            return@coroutineScope
        }

        val jobs = mirrorUrls.map { url ->
            async {
                if (isMirrorAlive(url)) url else null
            }
        }

        val best = jobs.awaitAll().firstOrNull { it != null } ?: ""
        if (best.isNotEmpty()) activeBaseUrl = best
    }

    private suspend fun <T> tryWithMirrors(block: suspend (mirror: String) -> T): T? {
        initializationJob?.join() // Asegurarnos de que terminó la inicialización mínima

        // Intentar con el mirror activo
        val active = activeBaseUrl
        if (active.isNotEmpty()) {
            try {
                return block(active)
            } catch (_: Exception) {}
        }

        // Si falla, probar todos los demás
        for (i in mirrorUrls.indices) {
            val mirror = mirrorUrls[i]
            if (mirror == active) continue
            try {
                val result = block(mirror)
                activeBaseUrl = mirror
                return result
            } catch (_: Exception) {}
        }

        // Si todos fallan catastróficamente, reiniciamos todo una vez
        webViewScraper.limpiarWebViewStorage()
        initializeMirrors()
        
        val finalActive = activeBaseUrl
        if (finalActive.isNotEmpty()) {
            try {
                return block(finalActive)
            } catch (_: Exception) {}
        }
        
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
            if (html.isEmpty()) throw Exception("No se obtuvo contenido")
            
            val libros = Jsoup.parseBodyFragment(html).body().children().toLibros()

            if (libros.isNotEmpty()) {
                searchCache.put(cacheKey, libros)
            }
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
                if (html.isEmpty()) throw Exception("No se obtuvo contenido")
                
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
                if (enlaceDescarga.isNotEmpty()) {
                    detailsCache.put(enlace, pair)
                }
                pair
            }

            result ?: Pair("Error al obtener detalles", emptyList())
        }
}