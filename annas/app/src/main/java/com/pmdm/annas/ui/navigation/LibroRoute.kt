package com.pmdm.annas.ui.navigation

import android.net.Uri
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.pmdm.annas.model.Libro
import com.pmdm.annas.ui.features.libro.LibroEvent
import com.pmdm.annas.ui.features.libro.LibroScreen
import com.pmdm.annas.ui.features.libro.LibroViewModel
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
        val libro = Json.decodeFromString<Libro>(Uri.decode(route.libroJson))
        val vm: LibroViewModel = hiltViewModel()

        LaunchedEffect(libro.enlace) {
            vm.onLibroEvent(LibroEvent.ObtenerLinksServidor(libro.enlace))
        }

        LibroScreen(
            libro = libro,
            descripcion = vm.descripcion,
            uiStateEnum = vm.uiStateEnum,
            enlacesServidor = vm.enlacesServidor,
            onReintentar = {
                vm.onLibroEvent(LibroEvent.ObtenerLinksServidor(libro.enlace))
            },
            onNavigateBack = onNavigateBack,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = this,
            okHttpClient = vm.okHttpClient
        )
    }
}
