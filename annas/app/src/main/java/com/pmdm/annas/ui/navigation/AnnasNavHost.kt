package com.pmdm.annas.ui.navigation

import android.net.Uri
import android.os.Build
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AnnasNavHost() {
    val navController: NavHostController = rememberNavController()

    val isAndroid16 = Build.VERSION.SDK_INT >= 36

    val fluidSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val offsetSpring = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = BuscarLibroRoute,

            // ANDROID 16+: física completa + predictive back
            // APIs antiguas: fallback compatible
            enterTransition = {
                if (isAndroid16) {
                    fadeIn(animationSpec = fluidSpring) +
                            slideInHorizontally(
                                initialOffsetX = { it / 8 },
                                animationSpec = offsetSpring
                            ) +
                            scaleIn(
                                initialScale = 0.9f,
                                animationSpec = fluidSpring
                            )
                } else {
                    fadeIn(animationSpec = fluidSpring) +
                            slideInHorizontally(
                                initialOffsetX = { it / 4 },
                                animationSpec = offsetSpring
                            )
                }
            },

            // SALIR ADELANTE: La pantalla anterior se aleja con efecto paralaje mejorado
            exitTransition = {
                if (isAndroid16) {
                    fadeOut(animationSpec = fluidSpring) +
                            slideOutHorizontally(
                                targetOffsetX = { -it / 8 },
                                animationSpec = offsetSpring
                            ) +
                            scaleOut(
                                targetScale = 1.1f,
                                animationSpec = fluidSpring
                            )
                } else {
                    fadeOut(animationSpec = fluidSpring) +
                            slideOutHorizontally(
                                targetOffsetX = { -it / 4 },
                                animationSpec = offsetSpring
                            )
                }
            },

            // VOLVER ATRÁS: La lista regresa con inercia física refinada
            popEnterTransition = {
                if (isAndroid16) {
                    fadeIn(animationSpec = fluidSpring) +
                            slideInHorizontally(
                                initialOffsetX = { -it / 8 },
                                animationSpec = offsetSpring
                            ) +
                            scaleIn(
                                initialScale = 1.1f,
                                animationSpec = fluidSpring
                            )
                } else {
                    fadeIn(animationSpec = fluidSpring) +
                            slideInHorizontally(
                                initialOffsetX = { -it / 4 },
                                animationSpec = offsetSpring
                            )
                }
            },

            // SALIR ATRÁS: Sincronización perfecta con gestos predictivos de API 36
            popExitTransition = {
                if (isAndroid16) {
                    fadeOut(animationSpec = fluidSpring) +
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = offsetSpring
                            ) +
                            scaleOut(
                                targetScale = 0.9f,
                                animationSpec = fluidSpring
                            )
                } else {
                    fadeOut(animationSpec = fluidSpring) +
                            slideOutHorizontally(
                                targetOffsetX = { it / 4 },
                                animationSpec = offsetSpring
                            )
                }
            }
        ) {
            buscarLibroDestination(
                onLibroClick = { libro ->
                    val libroJson = Uri.encode(Json.encodeToString(libro))
                    navController.navigate(LibroRoute(libroJson))
                },
                sharedTransitionScope = this@SharedTransitionLayout
            )

            libroDestination(
                onNavigateBack = {
                    navController.popBackStack()
                },
                sharedTransitionScope = this@SharedTransitionLayout
            )
        }
    }
}
