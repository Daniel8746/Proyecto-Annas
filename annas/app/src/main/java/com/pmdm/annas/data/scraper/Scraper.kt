package com.pmdm.annas.data.scraper

import com.pmdm.annas.data.repositorys.toLibros
import com.pmdm.annas.model.Libro
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class Scraper @Inject constructor(
    private val webViewScraper: WebViewScraper
) {
    private val baseUrl = "https://es.annas-archive.org"

    suspend fun buscarLibro(nombreLibro: String): List<Libro> {
        val cssSelector = "div.flex.pt-3.pb-3.border-b"

        webViewScraper.cssSelector = cssSelector

        val html = webViewScraper.loadUrlAndGetHtml("$baseUrl/search?q=$nombreLibro")

        return withContext(Dispatchers.IO) {
            val doc = Jsoup.parse(html)

            doc.select(cssSelector).toLibros()
        }
    }


    suspend fun servidorDescarga(enlace: String): Pair<String, List<String>> {
        val cssSelector = "div.mt-4.js-md5-top-box-description"

        webViewScraper.cssSelector = cssSelector

        val html = webViewScraper.loadUrlAndGetHtml("$baseUrl/$enlace")

        return withContext(Dispatchers.IO) {
            val doc = Jsoup.parse(html)

            val enlaceDescarga = mutableListOf<String>()

            val descripcion = doc
                .selectFirst(cssSelector)!!
                .select("div.mb-1")
                .text().trim()

            doc
                .select("ul.list-inside.mb-4.ml-1")[1]
                .select("li")
                .forEach {
                    enlaceDescarga.add(it.selectFirst("a")!!.attr("href"))
                }

            Pair(descripcion, enlaceDescarga)
        }
    }
}