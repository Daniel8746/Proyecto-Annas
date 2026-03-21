package com.pmdm.annas.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Libro(
    val enlace: String = "",
    val titulo: String = "",
    val autor: String = "",
    val portada: String = "",
    val formato: String = "",
    val tamano: String = "",
    val idioma: String = ""
) : Parcelable