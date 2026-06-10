package com.annas.ui.navigation

import android.net.Uri
import android.os.Build
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.annas.data.sensor.ShakeDetector
import kotlinx.serialization.json.Json

private val FluidSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMediumLow
)

private val OffsetSpring = spring<IntOffset>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMediumLow
)

@Composable
fun AnnasNavHost(shakeDetector: ShakeDetector) {
    val navController: NavHostController = rememberNavController()
    val isAndroid16 = Build.VERSION.SDK_INT >= 36

    LaunchedEffect(Unit) {
        shakeDetector.onShakeEvent.collect {
            val isAlreadyOnQr = navController.currentBackStackEntry?.destination?.hasRoute<QrRoute>() == true
            val canNavigate = navController.currentBackStackEntry
                ?.lifecycle
                ?.currentState
                ?.isAtLeast(Lifecycle.State.RESUMED) == true

            if (!isAlreadyOnQr && canNavigate) {
                runCatching {
                    navController.navigate(QrRoute) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    }

    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = BuscarLibroRoute,

            // ANDROID 16+: física completa + predictive back
            // APIs antiguas: fallback compatible
            enterTransition = {
                if (isAndroid16) {
                    fadeIn(animationSpec = FluidSpring) +
                            slideInHorizontally(
                                initialOffsetX = { it / 8 },
                                animationSpec = OffsetSpring
                            ) +
                            scaleIn(
                                initialScale = 0.9f,
                                animationSpec = FluidSpring
                            )
                } else {
                    fadeIn(animationSpec = FluidSpring) +
                            slideInHorizontally(
                                initialOffsetX = { it / 4 },
                                animationSpec = OffsetSpring
                            )
                }
            },

            // SALIR ADELANTE: La pantalla anterior se aleja con efecto paralaje mejorado
            exitTransition = {
                if (isAndroid16) {
                    fadeOut(animationSpec = FluidSpring) +
                            slideOutHorizontally(
                                targetOffsetX = { -it / 8 },
                                animationSpec = OffsetSpring
                            ) +
                            scaleOut(
                                targetScale = 1.1f,
                                animationSpec = FluidSpring
                            )
                } else {
                    fadeOut(animationSpec = FluidSpring) +
                            slideOutHorizontally(
                                targetOffsetX = { -it / 4 },
                                animationSpec = OffsetSpring
                            )
                }
            },

            // VOLVER ATRÁS: La lista regresa con inercia física refinada
            popEnterTransition = {
                if (isAndroid16) {
                    fadeIn(animationSpec = FluidSpring) +
                            slideInHorizontally(
                                initialOffsetX = { -it / 8 },
                                animationSpec = OffsetSpring
                            ) +
                            scaleIn(
                                initialScale = 1.1f,
                                animationSpec = FluidSpring
                            )
                } else {
                    fadeIn(animationSpec = FluidSpring) +
                            slideInHorizontally(
                                initialOffsetX = { -it / 4 },
                                animationSpec = OffsetSpring
                            )
                }
            },

            // SALIR ATRÁS: Sincronización perfecta con gestos predictivos de API 36
            popExitTransition = {
                if (isAndroid16) {
                    fadeOut(animationSpec = FluidSpring) +
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = OffsetSpring
                            ) +
                            scaleOut(
                                targetScale = 0.9f,
                                animationSpec = FluidSpring
                            )
                } else {
                    fadeOut(animationSpec = FluidSpring) +
                            slideOutHorizontally(
                                targetOffsetX = { it / 4 },
                                animationSpec = OffsetSpring
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

            qrDestination(
                onNavigateBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(BuscarLibroRoute) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}
