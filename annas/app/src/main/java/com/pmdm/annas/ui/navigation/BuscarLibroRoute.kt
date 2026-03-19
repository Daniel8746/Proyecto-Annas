package com.pmdm.annas.ui.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.pmdm.annas.model.Libro
import com.pmdm.annas.ui.features.buscarLibro.BuscarLibroScreen
import com.pmdm.annas.ui.features.buscarLibro.BuscarLibroViewModel
import kotlinx.serialization.Serializable

@Serializable
object BuscarLibroRoute

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.buscarLibroDestination(
    onNavigateToLibro: (Libro) -> Unit,
    sharedTransitionScope: SharedTransitionScope
) {
    composable<BuscarLibroRoute> {
        val vm: BuscarLibroViewModel = hiltViewModel()
        BuscarLibroScreen(
            buscarNombre = vm.buscar,
            uiState = vm.uistateEnum,
            libros = vm.libros,
            selectedExtensions = vm.selectedExtensions,
            selectedLanguage = vm.selectedLanguage,
            onBuscarLibroEvent = { vm.onBuscarLibroEvent(it) },
            onLibroClick = onNavigateToLibro,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = this
        )
    }
}
