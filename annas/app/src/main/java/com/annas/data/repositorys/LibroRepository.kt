package com.annas.data.repositorys

import com.annas.data.scraper.Scraper
import javax.inject.Inject

class LibroRepository @Inject constructor(
    private val scraper: Scraper
) {
    suspend fun getLinksServidor(enlace: String): Pair<String, List<String>> {
        return scraper.servidorDescarga(enlace)
    }
}