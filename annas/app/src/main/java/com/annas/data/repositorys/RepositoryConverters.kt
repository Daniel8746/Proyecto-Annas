package com.annas.data.repositorys

import com.annas.data.exceptions.LibroParseException
import com.annas.model.Libro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

private val sizeRegex =
    Regex("""\d+(?:[.,]\d+)?\s*(?:B|KB|MB|GB|KIB|MIB|GIB)""", RegexOption.IGNORE_CASE)
private val languageRegex =
    Regex("""\[[a-z]{2,3}(?:-[a-z]{2})?]""", RegexOption.IGNORE_CASE)
private val infoSeparatorRegex = Regex("\\s*(?:\\u00C2?\\u00B7|[|])\\s*")

fun Element.toLibro(): Libro {
    val tituloTag = selectFirst("a.text-lg")
    val enlace = tituloTag?.attr("href").orEmpty()

    if (tituloTag == null || enlace.isBlank()) {
        throw LibroParseException("El libro esta vacio o incompleto")
    }

    val autorTag = select("a.text-sm[href], a[href*=\"/search?\"]")
        .firstOrNull { it != tituloTag && it.text().isNotBlank() }
    val imgTag = selectFirst("img")

    val infoParts = extractInfoParts()
    val sizeIndex = infoParts.indexOfFirst { sizeRegex.containsMatchIn(it) }

    var idioma = "Desconocido"
    var formato = "Desconocido"
    var tamano = "Desconocido"

    if (sizeIndex != -1) {
        tamano = infoParts[sizeIndex]

        if (sizeIndex > 0) {
            val possibleFormat = infoParts[sizeIndex - 1]

            if (languageRegex.containsMatchIn(possibleFormat)) {
                idioma = infoParts.subList(0, sizeIndex).joinToString("\n")
            } else {
                formato = possibleFormat
                idioma = if (sizeIndex > 1) {
                    infoParts.subList(0, sizeIndex - 1).joinToString("\n")
                } else {
                    "Desconocido"
                }
            }
        }
    } else {
        idioma = infoParts.getOrNull(0) ?: "Desconocido"
        formato = infoParts.getOrNull(1) ?: "Desconocido"
    }

    return Libro(
        enlace = enlace,
        titulo = tituloTag.text().trim(),
        autor = autorTag?.text()?.trim().orEmpty().ifBlank { "Desconocido" },
        portada = imgTag?.let { img -> img.attr("src").ifBlank { img.attr("data-src") } }.orEmpty(),
        idioma = idioma.ifBlank { "Desconocido" },
        formato = formato.ifBlank { "Desconocido" },
        tamano = tamano.ifBlank { "Desconocido" }
    )
}

private fun Element.extractInfoParts(): List<String> {
    val candidates = select(
        "div.text-gray-800.font-semibold.text-sm, " +
                "div.dark\\:text-slate-400.font-semibold.text-sm, " +
                "div[class*=\"font-semibold\"][class*=\"text-sm\"]"
    )

    val infoText = candidates
        .map { it.text().trim() }
        .firstOrNull { sizeRegex.containsMatchIn(it) }
        ?: candidates.text().trim()

    return infoText
        .split(infoSeparatorRegex)
        .map { it.trim() }
        .filter { it.isNotBlank() }
}

fun Elements.toLibros(): List<Libro> = mapNotNull {
    try {
        it.toLibro()
    } catch (_: Exception) {
        null
    }
}.distinctBy { it.enlace }

inline fun <T> MutableStateFlow<T>.updateState(
    block: T.() -> T
) {
    update(block)
}
