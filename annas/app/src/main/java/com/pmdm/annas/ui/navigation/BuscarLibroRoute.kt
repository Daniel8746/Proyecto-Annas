package com.pmdm.annas.ui.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
    onLibroClick: (Libro) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    composable<BuscarLibroRoute> {
        val viewModel: BuscarLibroViewModel = hiltViewModel()

        BuscarLibroScreen(
            uiState = viewModel.uiState,
            onBuscarLibroEvent = viewModel::onBuscarLibroEvent,
            onLibroClick = onLibroClick,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
}
