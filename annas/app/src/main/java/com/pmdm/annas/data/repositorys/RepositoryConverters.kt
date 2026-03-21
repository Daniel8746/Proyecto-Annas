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

    // Scraper actualizado con las nuevas clases de Anna's Archive
    val infoText =
        this.select("div.text-gray-800.font-semibold.text-sm, div.dark\\:text-slate-400.font-semibold.text-sm")
            .text()

    // Dividimos por el punto medio (·) que separa las secciones principales
    val mainParts = infoText.split("·").map { it.trim() }.filter { it.isNotBlank() }

    // Regex para identificar el tamaño del archivo (ej: "0.4MB", "10.2 KB", "1.5GB")
    val sizeRegex = Regex("""\d+(?:\.\d+)?\s*(?:B|KB|MB|GB)""", RegexOption.IGNORE_CASE)
    val sizeIndex = mainParts.indexOfFirst { sizeRegex.containsMatchIn(it) }

    var idioma = "Desconocido"
    var formato = "Desconocido"
    var tamano = "Desconocido"

    if (sizeIndex != -1) {
        // 1. El tamaño es el elemento que coincide con el Regex
        tamano = mainParts[sizeIndex]

        if (sizeIndex > 0) {
            val potentialFormat = mainParts[sizeIndex - 1]

            // 2. Determinamos el formato e idiomas
            // Los idiomas en Anna's suelen llevar el código entre corchetes: [en], [es], [fr]
            if (potentialFormat.contains(Regex("""\[\w{2,3}]"""))) {
                // Si el elemento anterior al tamaño parece un idioma, el formato es desconocido o no se indica
                // Usamos salto de línea para mejorar la estética cuando hay varios idiomas
                idioma = mainParts.subList(0, sizeIndex).joinToString("\n")
                formato = "Desconocido"
            } else {
                // Caso estándar: Idioma(s) · Formato · Tamaño
                formato = potentialFormat
                idioma = if (sizeIndex > 1) {
                    // Todos los elementos antes del formato son idiomas, usamos salto de línea
                    mainParts.subList(0, sizeIndex - 1).joinToString("\n")
                } else {
                    "Desconocido"
                }
            }
        }
    } else {
        // Fallback en caso de que la estructura sea distinta
        idioma = mainParts.getOrNull(0) ?: "Desconocido"
        formato = mainParts.getOrNull(1) ?: "Desconocido"
    }

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

fun Elements.toLibros(): List<Libro> = mapNotNull {
    try {
        it.toLibro()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
