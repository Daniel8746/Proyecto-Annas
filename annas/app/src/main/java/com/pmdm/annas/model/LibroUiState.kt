package com.pmdm.annas.model

import com.pmdm.annas.ui.features.UIStateEnum

data class LibroUiState(
    val descripcion: String = "",
    val enlacesServidor: List<String> = emptyList(),
    val uiStateEnum: UIStateEnum? = null
)