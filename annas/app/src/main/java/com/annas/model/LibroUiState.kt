package com.annas.model

import androidx.compose.runtime.Immutable
import com.annas.ui.features.UIStateEnum
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class LibroUiState(
    val descripcion: String = "",
    val enlacesServidor: PersistentList<String> = persistentListOf(),
    val uiStateEnum: UIStateEnum? = null
)
