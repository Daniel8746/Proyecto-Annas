package com.pmdm.annas.data.scraper

import android.content.Context
import android.util.LruCache
import androidx.core.content.edit
import com.pmdm.annas.data.repositorys.toLibros
import com.pmdm.annas.model.Libro
import com.pmdm.annas.uri.UriUtils
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.Collections
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

    private val mirrorUrls = Collections.synchronizedList(mutableListOf<String>())
    private var activeMirrorIndex = 0
    private val urlMirror = "https://shadowlibraries.github.io/DirectDownloads/AnnasArchive/"

    private val searchCache = LruCache<String, List<Libro>>(200)
    private val detailsCache = LruCache<String, Pair<String, List<String>>>(400)
    private val cacheLock = Mutex()

    private var activeBaseUrl: String
        get() = mirrorUrls.getOrNull(activeMirrorIndex)
            ?: prefs.getString("mirror", "")
            ?: ""
        set(value) {
            val index = mirrorUrls.indexOf(value)
            if (index >= 0) activeMirrorIndex = index
            prefs.edit { putString("mirror", value) }
        }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var initializationJob: Job? = null

    private companion object {
        const val SEARCH_SELECTOR = "div.flex.pt-3.pb-3.border-b"
        const val DETAILS_SELECTOR = "main, .js-md5-top-box-description, h3, ul"
    }

    init {
        val savedMirrors = prefs.getStringSet("mirror_list", emptySet())

        if (!savedMirrors.isNullOrEmpty()) {
            mirrorUrls.addAll(savedMirrors)

            val lastMirror = prefs.getString("mirror", "") ?: ""
            activeMirrorIndex =
                mirrorUrls.indexOf(lastMirror).takeIf { it >= 0 } ?: 0
        }

        initializationJob = scope.launch {
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
        return try {
            val request = Request.Builder().url(url).head().build()

            okHttpClient.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (_: Exception) {
            false
        }
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
                val html =
                    webViewScraper.loadUrlAndGetHtml(urlMirror, cssSelector)

                if (html.isEmpty()) throw Exception("HTML vacío")

                val doc = Jsoup.parse(html)

                val newMirrors = mutableListOf<String>()

                doc.select("li").forEach { li ->
                    val href =
                        li.selectFirst("a")
                            ?.attr("href")
                            ?.substringBefore("/?")
                            ?: ""

                    if (href.startsWith("http")) {
                        newMirrors.add(href)
                    }
                }

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

        for (mirror in mirrorUrls) {
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
        val query = nombreLibro.trim()

        if (query.isEmpty()) return@withContext emptyList()

        val cacheKey =
            "$query-${extensiones.sorted().joinToString(separator = ",")}-$idioma-$pagina"

        cacheLock.withLock {
            searchCache.get(cacheKey)
        }?.let {
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

            val html =
                webViewScraper.loadUrlAndGetHtml(url, cssSelector)

            if (html.isEmpty()) throw Exception("No se obtuvo contenido")

            val libros =
                Jsoup.parseBodyFragment(html)
                    .body()
                    .children()
                    .toLibros()

            if (libros.isNotEmpty()) {
                cacheLock.withLock {
                    searchCache.put(cacheKey, libros)
                }
            }

            libros
        }

        result ?: emptyList()
    }

    suspend fun servidorDescarga(
        enlace: String
    ): Pair<String, List<String>> =
        withContext(Dispatchers.IO) {

            cacheLock.withLock {
                detailsCache.get(enlace)
            }?.let {
                return@withContext it
            }

            val cssSelector = DETAILS_SELECTOR

            val result = tryWithMirrors { mirror ->
                val url =
                    if (enlace.startsWith("/"))
                        "$mirror$enlace"
                    else enlace

                val html =
                    webViewScraper.loadUrlAndGetHtml(url, cssSelector)

                if (html.isEmpty())
                    throw Exception("No se obtuvo contenido")

                val doc = Jsoup.parse(html)

                val enlaces = mutableListOf<String>()

                val descripcion =
                    doc.select(".js-md5-top-box-description div.mb-1")
                        .firstOrNull()
                        ?.text()
                        ?.trim()
                        ?: "Sin descripción"

                val slowHeader =
                    doc.select("h3")
                        .find {
                            it.text().contains(
                                "Slow downloads",
                                ignoreCase = true
                            )
                        }

                val ulLista =
                    slowHeader
                        ?.parent()
                        ?.select("ul.list-inside")
                        .orEmpty()
                        .plus(
                            slowHeader
                                ?.nextElementSiblings()
                                ?.select("ul.list-inside")
                                .orEmpty()
                        )
                        .firstOrNull()

                ulLista
                    ?.select("li")
                    ?.forEach { li ->
                        val text =
                            li.text().lowercase()

                        if (
                            text.contains("partner server")
                            && text.contains("no waitlist")
                        ) {
                            val href =
                                li.selectFirst("a")
                                    ?.attr("href")
                                    ?: ""

                            if (href.isNotEmpty()) {
                                val fullUrl =
                                    if (href.startsWith("/"))
                                        "$mirror$href"
                                    else href

                                if (!enlaces.contains(fullUrl)) {
                                    enlaces.add(fullUrl)
                                }
                            }
                        }
                    }

                val pair =
                    Pair(descripcion, enlaces)

                if (enlaces.isNotEmpty()) {
                    cacheLock.withLock {
                        detailsCache.put(enlace, pair)
                    }
                }

                pair
            }

            result ?: Pair(
                "Error al obtener detalles",
                emptyList()
            )
        }
}