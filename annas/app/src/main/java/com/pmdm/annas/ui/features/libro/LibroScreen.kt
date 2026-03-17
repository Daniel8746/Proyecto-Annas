package com.pmdm.annas.ui.features.libro

import androidx.compose.runtime.Composable
import com.pmdm.annas.model.Libro
import com.pmdm.annas.ui.features.UIStateEnum
import com.pmdm.annas.ui.features.components.ErrorScreen
import com.pmdm.annas.ui.features.components.PantallaCarga
import com.pmdm.annas.ui.features.libro.components.MostrarLibro

@Composable
fun LibroScreen(
    libro: Libro,
    descripcion: String,
    uiStateEnum: UIStateEnum?,
    enlacesServidor: List<String>,
    onReintentar: () -> Unit
) {
    when (uiStateEnum) {
        UIStateEnum.CARGANDO -> PantallaCarga()

        UIStateEnum.CARGADO -> MostrarLibro(
            portada = libro.portada,
            titulo = libro.titulo,
            autor = libro.autor,
            descripcion = descripcion,
            enlacesServidor = enlacesServidor
        )

        else -> ErrorScreen(
            mensaje = "Error al abrir el libro",
            onReintentar = onReintentar
        )
    }
}