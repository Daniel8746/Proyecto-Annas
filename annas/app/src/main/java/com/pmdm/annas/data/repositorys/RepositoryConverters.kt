package com.pmdm.annas.data.repositorys

import com.pmdm.annas.data.exceptions.LibroParseException
import com.pmdm.annas.model.Libro
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

// Libro
fun Element.toLibro(): Libro {
    val tituloTag = this.selectFirst("a.text-lg")
    val autorTag = this.selectFirst("a.text-sm")
    val imgTag = this.selectFirst("img")

    if (tituloTag != null && autorTag != null && imgTag != null) {
        return Libro(
            enlace = tituloTag.attr("href"),
            titulo = tituloTag.text().trim(),
            autor = autorTag.text().trim(),
            portada = imgTag.attr("src")
        )
    }

    throw LibroParseException("El libro está vacío o incompleto")
}

fun Elements.toLibros(): List<Libro> = map { it.toLibro() }
