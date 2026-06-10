package com.annas.model

import androidx.compose.runtime.Immutable
import com.annas.ui.features.UIStateEnum
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

@Immutable
data class BuscarLibroUiState(
    val libros: PersistentList<Libro> = persistentListOf(),
    val buscar: String = "",
    val selectedExtensions: PersistentSet<String> = persistentSetOf(),
    val selectedLanguage: String? = null,
    val pagina: Int = 1,
    val uiStateEnum: UIStateEnum? = null
)
