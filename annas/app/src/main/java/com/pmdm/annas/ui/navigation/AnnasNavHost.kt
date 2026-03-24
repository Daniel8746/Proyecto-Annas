package com.pmdm.annas.ui.navigation

import android.net.Uri
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AnnasNavHost() {
    val navController: NavHostController = rememberNavController()
    
    // Curva de "Respuesta Inmediata" (Material 3 Emphasized Decelerate)
    // Empieza con mucha energía y se asienta con una suavidad extrema.
    val ultraFluidEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val duration = 400 // 400ms es el "Sweet Spot" para que se note pero no sea lento.

    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = BuscarLibroRoute,
            
            // AVANZAR: La nueva pantalla entra con un leve empuje y escala desde el fondo.
            enterTransition = {
                fadeIn(animationSpec = tween(duration, easing = ultraFluidEasing)) +
                slideInHorizontally(
                    initialOffsetX = { it / 10 }, // Leve parallax (10%) para dar dirección
                    animationSpec = tween(duration, easing = ultraFluidEasing)
                ) +
                scaleIn(
                    initialScale = 0.92f, 
                    animationSpec = tween(duration, easing = ultraFluidEasing)
                )
            },
            
            // SALIR ADELANTE: La lista se retrae y se aleja sutilmente.
            exitTransition = {
                fadeOut(animationSpec = tween(duration, easing = ultraFluidEasing)) +
                slideOutHorizontally(
                    targetOffsetX = { -it / 10 }, 
                    animationSpec = tween(duration, easing = ultraFluidEasing)
                ) +
                scaleOut(
                    targetScale = 1.08f, 
                    animationSpec = tween(duration, easing = ultraFluidEasing)
                )
            },
            
            // VOLVER ATRÁS: La lista "regresa" desde el fondo.
            popEnterTransition = {
                fadeIn(animationSpec = tween(duration, easing = ultraFluidEasing)) +
                slideInHorizontally(
                    initialOffsetX = { -it / 10 },
                    animationSpec = tween(duration, easing = ultraFluidEasing)
                ) +
                scaleIn(
                    initialScale = 1.08f, 
                    animationSpec = tween(duration, easing = ultraFluidEasing)
                )
            },
            
            // SALIR ATRÁS: La pantalla sale deslizándose hacia la derecha.
            // Esto encaja perfectamente con el gesto de atrás del sistema.
            popExitTransition = {
                fadeOut(animationSpec = tween(duration, easing = ultraFluidEasing)) +
                slideOutHorizontally(
                    targetOffsetX = { it }, // Sale completamente para revelar la lista
                    animationSpec = tween(duration, easing = ultraFluidEasing)
                ) +
                scaleOut(
                    targetScale = 0.92f, 
                    animationSpec = tween(duration, easing = ultraFluidEasing)
                )
            }
        ) {
            buscarLibroDestination(
                onNavigateToLibro = { libro ->
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
