package com.annas.ui.navigation

import android.net.Uri
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.annas.model.Libro
import com.annas.ui.features.libro.LibroEvent
import com.annas.ui.features.libro.LibroScreen
import com.annas.ui.features.libro.LibroViewModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class LibroRoute(val libroJson: String)

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.libroDestination(
    onNavigateBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope
) {
    composable<LibroRoute> { backStackEntry ->
        val route: LibroRoute = backStackEntry.toRoute()
        val libro = remember(route.libroJson) {
            Json.decodeFromString<Libro>(Uri.decode(route.libroJson))
        }
        val vm: LibroViewModel = hiltViewModel()

        LaunchedEffect(libro.enlace) {
            vm.onLibroEvent(LibroEvent.ObtenerLinksServidor(libro.enlace))
        }

        val uiState by vm.uiState.collectAsStateWithLifecycle()
        val tiempoEspera by vm.tiempoEspera.collectAsStateWithLifecycle()
        val downloadState by vm.downloadState.collectAsStateWithLifecycle()

        LibroScreen(
            libro = libro,
            descripcion = uiState.descripcion,
            uiStateEnum = uiState.uiStateEnum,
            tiempoEspera = tiempoEspera,
            enlacesServidor = uiState.enlacesServidor,
            downloadState = downloadState,
            onLibroEvent = vm::onLibroEvent,
            onNavigateBack = onNavigateBack,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = this
        )
    }
}
