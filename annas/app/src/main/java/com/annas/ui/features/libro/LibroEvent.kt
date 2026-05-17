package com.annas.ui.features.libro

import android.app.Activity
import android.net.Uri

sealed interface LibroEvent {
    data class ObtenerLinksServidor(val enlace: String) : LibroEvent
    data class PrepararDescarga(val context: Activity, val url: String) : LibroEvent
    data class DescargarLibro(val fileUri: Uri?) : LibroEvent
}