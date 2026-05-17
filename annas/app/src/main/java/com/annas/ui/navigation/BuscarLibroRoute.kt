package com.annas.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.annas.model.Libro
import com.annas.ui.features.buscarLibro.BuscarLibroScreen
import com.annas.ui.features.buscarLibro.BuscarLibroViewModel
import kotlinx.serialization.Serializable

@Serializable
object BuscarLibroRoute

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.buscarLibroDestination(
    onLibroClick: (Libro) -> Unit,
    sharedTransitionScope: SharedTransitionScope
) {
    composable<BuscarLibroRoute> {
        val vm: BuscarLibroViewModel = hiltViewModel()

        val uiState by vm.uiState.collectAsStateWithLifecycle()

        BuscarLibroScreen(
            uiState = uiState,
            onBuscarLibroEvent = vm::onBuscarLibroEvent,
            onLibroClick = onLibroClick,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = this
        )
    }
}
