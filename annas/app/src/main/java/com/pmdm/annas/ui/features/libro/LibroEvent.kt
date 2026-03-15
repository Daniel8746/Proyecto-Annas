package com.pmdm.annas.ui.features.libro

sealed interface LibroEvent {
    data class ObtenerLinksServidor(val enlace: String): LibroEvent
}