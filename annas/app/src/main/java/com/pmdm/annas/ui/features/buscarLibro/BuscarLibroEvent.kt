package com.pmdm.annas.ui.features.buscarLibro

sealed interface BuscarLibroEvent {
    data object OnClickBuscar: BuscarLibroEvent
    data class OnBuscarChange(val nombre: String): BuscarLibroEvent
    data class OnClickLibro(val onNavigateLibro: () -> Unit): BuscarLibroEvent
    data class OnToggleExtension(val ext: String): BuscarLibroEvent
    data class OnIdiomaChange(val idioma: String?): BuscarLibroEvent
}
