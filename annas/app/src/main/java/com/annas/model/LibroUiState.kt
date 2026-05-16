package com.annas.model

import com.annas.ui.features.UIStateEnum

data class LibroUiState(
    val descripcion: String = "",
    val enlacesServidor: List<String> = emptyList(),
    val uiStateEnum: UIStateEnum? = null
)