package com.pmdm.annas.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.pmdm.annas.model.Libro
import com.pmdm.annas.ui.features.buscarLibro.BuscarLibroScreen
import com.pmdm.annas.ui.features.buscarLibro.BuscarLibroViewModel
import kotlinx.serialization.Serializable

@Serializable
object BuscarLibroRoute

fun NavGraphBuilder.buscarLibroDestination(
    vm: BuscarLibroViewModel,
    setLibroSeleccionado: (Libro) -> Unit,
    onNavigateToLibro: () -> Unit
) {
    composable<BuscarLibroRoute> {
        BuscarLibroScreen(
            buscarNombre = vm.buscar,
            uiState = vm.uistateEnum,
            libros = vm.libros,
            onBuscarLibroEvent = { vm.onBuscarLibroEvent(it) },
            setLibroSeleccionado = setLibroSeleccionado,
            onNavigateToLibro = onNavigateToLibro
        )
    }
}