package com.pmdm.annas.data.scraper

import android.util.Log
import android.util.LruCache
import com.pmdm.annas.data.repositorys.toLibros
import com.pmdm.annas.model.Libro
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Scraper @Inject constructor(
    private val webViewScraper: WebViewScraper,
    private val okHttpClient: OkHttpClient
) {
    private var activeBaseUrl: String = ""
    private val mirrorUrls = listOf(
        "https://annas-archive.gl",
        "https://annas-archive.pk",
        "https://annas-archive.gd"
    )

    private val searchCache = LruCache<String, List<Libro>>(20)
    private val detailsCache = LruCache<String, Pair<String, List<String>>>(50)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            findBestMirror()
        }
    }

    private fun findBestMirror() {
        for (url in mirrorUrls) {
            try {
                val request = Request.Builder().url(url).head().build()
                val response = okHttpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    activeBaseUrl = url
                    Log.d("Scraper", "URL activa seleccionada: $activeBaseUrl")
                    return
                }
            } catch (e: Exception) {
                Log.e("Scraper", "Error probando mirror $url: ${e.message}")
            }
        }
    }

    suspend fun buscarLibro(
        nombreLibro: String,
        extensiones: List<String> = emptyList(),
        idioma: String? = null
    ): List<Libro> = withContext(Dispatchers.IO) {
        val query = nombreLibro.trim()
        if (query.isEmpty()) return@withContext emptyList()

        val cacheKey = "$query-${extensiones.sorted().joinToString(",")}-$idioma"
        searchCache.get(cacheKey)?.let { return@withContext it }

        val cssSelector = "div.flex.pt-3.pb-3.border-b"

        try {
            var url = "$activeBaseUrl/search?q=${UriEncoder.encode(query)}"
            extensiones.forEach { ext -> url += "&ext=$ext" }
            if (!idioma.isNullOrEmpty()) {
                url += "&lang=$idioma"
            }

            val html = webViewScraper.loadUrlAndGetHtml(url, cssSelector)
            val libros = Jsoup.parseBodyFragment(html).body().children().toLibros()

            if (libros.isNotEmpty()) {
                searchCache.put(cacheKey, libros)
            }
            libros
        } catch (e: Exception) {
            e.printStackTrace()
            findBestMirror()
            emptyList()
        }
    }

    suspend fun servidorDescarga(enlace: String): Pair<String, List<String>> =
        withContext(Dispatchers.IO) {
            detailsCache.get(enlace)?.let { return@withContext it }

            // Selector amplio para capturar toda la página de descarga
            val cssSelector = "main, .js-md5-top-box-description, h3, ul"

            try {
                val url = if (enlace.startsWith("/")) "$activeBaseUrl$enlace" else enlace
                val html = webViewScraper.loadUrlAndGetHtml(url, cssSelector)

                val doc = Jsoup.parse(html)
                val enlaceDescarga = mutableListOf<String>()

                val descripcion = doc.select(".js-md5-top-box-description div.mb-1")
                    .firstOrNull()?.text()?.trim() ?: "Sin descripción"

                // Buscamos la sección de "Slow downloads" de forma más robusta
                val slowHeader = doc.select("h3").find { it.text().contains("Slow downloads", ignoreCase = true) }
                
                // Buscamos la lista UL que contenga los "Partner Server"
                val ulLista = slowHeader?.parent()?.select("ul.list-inside").orEmpty()
                    .plus(slowHeader?.nextElementSiblings()?.select("ul.list-inside").orEmpty())
                    .firstOrNull()

                ulLista?.select("li a")?.forEach {
                    val text = it.text().lowercase()
                    if (text.contains("partner server")) {
                        val href = it.attr("href")
                        if (href.isNotEmpty()) {
                            val fullUrl = if (href.startsWith("/")) "$activeBaseUrl$href" else href
                            if (!enlaceDescarga.contains(fullUrl)) {
                                enlaceDescarga.add(fullUrl)
                            }
                        }
                    }
                }

                val result = Pair(descripcion, enlaceDescarga)
                if (enlaceDescarga.isNotEmpty()) {
                    detailsCache.put(enlace, result)
                }
                result
            } catch (e: Exception) {
                e.printStackTrace()
                Pair("Error al obtener detalles", emptyList())
            }
        }
}

object UriEncoder {
    fun encode(s: String): String = java.net.URLEncoder.encode(s, "UTF-8")
}
