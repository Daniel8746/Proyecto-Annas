package com.pmdm.annas.data.repositorys

import com.pmdm.annas.data.scraper.Scraper
import com.pmdm.annas.model.Libro
import javax.inject.Inject

class BuscarLibroRepository @Inject constructor(
    private val scraper: Scraper
) {
    suspend fun getLibros(nombre: String): List<Libro> {
        return scraper.buscarLibro(nombre)
    }
}