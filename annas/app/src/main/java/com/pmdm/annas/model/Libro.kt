package com.pmdm.annas.model

import kotlinx.serialization.Serializable

@Serializable
data class Libro(
    val enlace: String = "",
    val titulo: String = "",
    val autor: String = "",
    val portada: String = "",
    val formato: String = "",
    val tamano: String = "",
    val idioma: String = ""
)