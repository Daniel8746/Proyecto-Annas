package com.annas.model

import android.os.Parcelable
import com.annas.enums.UIStateEnum
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import androidx.compose.runtime.Immutable

@Immutable
data class BuscarLibroUiState(
    val libros: PersistentList<Libro> = persistentListOf(),
    val buscar: String = "",
    val selectedExtensions: PersistentSet<String> = persistentSetOf(),
    val selectedLanguage: String? = null,
    val pagina: Int = 1,
    val uiStateEnum: UIStateEnum? = null
)

@Immutable
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

@Immutable
data class LibroDescargaUiState(
    val descripcion: String = "",
    val enlacesServidor: PersistentList<String> = persistentListOf(),
    val uiStateEnum: UIStateEnum? = null
)