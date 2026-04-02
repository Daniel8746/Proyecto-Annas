package com.pmdm.annas.model

import com.pmdm.annas.ui.features.UIStateEnum

data class BuscarLibroUiState(
    val libros: List<Libro> = emptyList(),
    val buscar: String = "",
    val selectedExtensions: List<String> = emptyList(),
    val selectedLanguage: String? = null,
    val pagina: Int = 1,
    val uiStateEnum: UIStateEnum? = null
)