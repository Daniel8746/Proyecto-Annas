package com.pmdm.annas.data.repositorys

import com.pmdm.annas.data.scraper.Scraper
import com.pmdm.annas.model.Libro
import javax.inject.Inject

class BuscarLibroRepository @Inject constructor(
    private val scraper: Scraper
) {
    suspend fun getLibros(
        nombre: String,
        extensiones: List<String> = emptyList(),
        idioma: String? = null,
        pagina: Int = 1
    ): List<Libro> {
        return scraper.buscarLibro(nombre, extensiones, idioma, pagina)
    }
}
