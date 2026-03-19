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
    
    // Scraper mejorado para sacar idioma, tamaño y formato
    val infoText = this.select("div.truncate.text-xs.text-gray-500").text()
    
    // El texto suele ser: "Spanish, pdf, 10.2MB, " o similar
    val parts = infoText.split(",").map { it.trim() }
    
    val idioma = parts.getOrNull(0) ?: "Desconocido"
    val formato = parts.getOrNull(1) ?: "Desconocido"
    val tamano = parts.getOrNull(2) ?: "Desconocido"

    if (tituloTag != null && autorTag != null && imgTag != null) {
        return Libro(
            enlace = tituloTag.attr("href"),
            titulo = tituloTag.text().trim(),
            autor = autorTag.text().trim(),
            portada = imgTag.attr("src"),
            idioma = idioma,
            formato = formato,
            tamano = tamano
        )
    }

    throw LibroParseException("El libro está vacío o incompleto")
}

fun Elements.toLibros(): List<Libro> = map { it.toLibro() }
