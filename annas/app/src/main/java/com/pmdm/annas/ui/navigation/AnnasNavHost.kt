package com.pmdm.annas.ui.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedVisibilityScope
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

    // Configuración de física para Android 16: Fluidez absoluta (System Physics Tuning)
    // Aumentamos la rigidez inicial para respuesta inmediata y bajamos el rebote para elegancia
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

            // AVANZAR: Entrada elástica sutil con mayor profundidad (Android 16 Style)
            enterTransition = {
                fadeIn(animationSpec = fluidSpring) +
                        slideInHorizontally(
                            initialOffsetX = { it / 8 },
                            animationSpec = offsetSpring
                        ) +
                        scaleIn(
                            initialScale = 0.9f,
                            animationSpec = fluidSpring
                        )
            },

            // SALIR ADELANTE: La pantalla anterior se aleja con efecto paralaje mejorado
            exitTransition = {
                fadeOut(animationSpec = fluidSpring) +
                        slideOutHorizontally(
                            targetOffsetX = { -it / 8 },
                            animationSpec = offsetSpring
                        ) +
                        scaleOut(
                            targetScale = 1.1f,
                            animationSpec = fluidSpring
                        )
            },

            // VOLVER ATRÁS: La lista regresa con inercia física refinada
            popEnterTransition = {
                fadeIn(animationSpec = fluidSpring) +
                        slideInHorizontally(
                            initialOffsetX = { -it / 8 },
                            animationSpec = offsetSpring
                        ) +
                        scaleIn(
                            initialScale = 1.1f,
                            animationSpec = fluidSpring
                        )
            },

            // SALIR ATRÁS: Sincronización perfecta con gestos predictivos de API 36
            popExitTransition = {
                fadeOut(animationSpec = fluidSpring) +
                        slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = offsetSpring
                        ) +
                        scaleOut(
                            targetScale = 0.9f,
                            animationSpec = fluidSpring
                        )
            }
        ) {
            buscarLibroDestination(
                onLibroClick = { libro ->
                    val libroJson = Uri.encode(Json.encodeToString(libro))
                    navController.navigate(LibroRoute(libroJson))
                },
                sharedTransitionScope = this@SharedTransitionLayout,
                animatedVisibilityScope = this as AnimatedVisibilityScope
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
