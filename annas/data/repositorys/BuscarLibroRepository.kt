package com.annas.data.repositorys

import com.annas.data.scraper.Scraper
import com.annas.model.Libro
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
        // Ahora pasamos el parámetro 'pagina' de forma modular
        return scraper.buscarLibro(nombre, extensiones, idioma, pagina)
    }
}
