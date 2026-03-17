package com.pmdm.annas.ui.navigation

import android.util.Log
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.pmdm.annas.model.Libro
import com.pmdm.annas.ui.features.libro.LibroEvent
import com.pmdm.annas.ui.features.libro.LibroScreen
import com.pmdm.annas.ui.features.libro.LibroViewModel
import kotlinx.serialization.Serializable

@Serializable
object LibroRoute

fun NavGraphBuilder.libroDestination(
    vm: LibroViewModel,
    libro: Libro
) {
    composable<LibroRoute> {
        fun onReintentar() {
            vm.onLibroEvent(LibroEvent.ObtenerLinksServidor(libro.enlace))
        }

        onReintentar()

        Log.d("Libro", libro.enlace)
        LibroScreen(
            libro = libro,
            descripcion = vm.descripcion,
            uiStateEnum = vm.uiStateEnum,
            enlacesServidor = vm.enlacesServidor,
            onReintentar = { onReintentar() }
        )
    }
}