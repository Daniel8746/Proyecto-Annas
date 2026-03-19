package com.pmdm.annas.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AnnasNavHost() {
    val navController: NavHostController = rememberNavController()

    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = BuscarLibroRoute,
            enterTransition = { fadeIn(animationSpec = tween(400)) },
            exitTransition = { fadeOut(animationSpec = tween(400)) },
            popEnterTransition = { fadeIn(animationSpec = tween(400)) },
            popExitTransition = { fadeOut(animationSpec = tween(400)) }
        ) {
            buscarLibroDestination(
                onNavigateToLibro = { libro ->
                    navController.navigate(LibroRoute(libro))
                },
                sharedTransitionScope = this@SharedTransitionLayout
            )

            libroDestination(
                onNavigateBack = {
                    navController.popBackStack(BuscarLibroRoute, inclusive = false)
                },
                sharedTransitionScope = this@SharedTransitionLayout
            )
        }
    }
}
