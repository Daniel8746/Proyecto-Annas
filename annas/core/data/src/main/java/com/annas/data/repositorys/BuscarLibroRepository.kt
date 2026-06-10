package com.annas.data.repositorys

import com.annas.data.scraper.Scraper
import com.annas.model.Libro
import kotlinx.collections.immutable.PersistentList
import javax.inject.Inject

class BuscarLibroRepository @Inject constructor(
    private val scraper: Scraper
) {
    suspend fun getLibros(
        nombre: String,
        extensiones: Set<String> = emptySet(),
        idioma: String? = null,
        pagina: Int = 1
    ): PersistentList<Libro> {
        return scraper.buscarLibro(nombre, extensiones, idioma, pagina)
    }
}
