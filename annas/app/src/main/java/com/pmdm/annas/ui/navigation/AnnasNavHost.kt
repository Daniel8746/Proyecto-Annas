package com.pmdm.annas.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.pmdm.annas.ui.features.LibroCompartidoViewModel
import com.pmdm.annas.ui.features.buscarLibro.BuscarLibroViewModel
import com.pmdm.annas.ui.features.libro.LibroViewModel

@Composable
fun AnnasNavHost() {
    val navController: NavHostController = rememberNavController()
    val buscarLibroViewModel = hiltViewModel<BuscarLibroViewModel>()
    val libroViewModel = hiltViewModel<LibroViewModel>()
    val libroCompartidoViewModel = hiltViewModel<LibroCompartidoViewModel>()

    // Esto permite interceptar el gesto de back
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    BackHandler {
        // Aquí decides si haces popBackStack o cancelas
        if (navController.previousBackStackEntry != null) {
            navController.popBackStack()
        } else {
            backDispatcher?.onBackPressed()
        }
    }

    NavHost(
        navController = navController,
        startDestination = BuscarLibroRoute,
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) },
        popEnterTransition = { fadeIn(animationSpec = tween(300)) },
        popExitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
        buscarLibroDestination(
            vm = buscarLibroViewModel,
            setLibroSeleccionado = { libroCompartidoViewModel.libroSeleccionado = it},
            onNavigateToLibro = { navController.navigate(LibroRoute) }
        )

        libroDestination(
            vm = libroViewModel,
            libro = libroCompartidoViewModel.libroSeleccionado
        )
    }
}