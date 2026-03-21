package com.pmdm.annas.ui.features.buscarLibro

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pmdm.annas.model.Libro
import com.pmdm.annas.ui.features.UIStateEnum
import com.pmdm.annas.ui.features.buscarLibro.components.Buscador
import com.pmdm.annas.ui.features.buscarLibro.components.MostrarLibros
import com.pmdm.annas.ui.features.buscarLibro.components.PantallaInicial
import com.pmdm.annas.ui.features.components.ErrorScreen
import com.pmdm.annas.ui.features.components.PantallaCarga

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BuscarLibroScreen(
    buscarNombre: String,
    uiState: UIStateEnum?,
    libros: List<Libro>,
    selectedExtensions: List<String>,
    selectedLanguage: String?,
    onBuscarLibroEvent: (BuscarLibroEvent) -> Unit,
    onLibroClick: (Libro) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    Scaffold(
        topBar = {
            Buscador(
                buscarNombre = buscarNombre,
                onValueChange = { onBuscarLibroEvent(BuscarLibroEvent.OnBuscarChange(it)) },
                onBuscar = { onBuscarLibroEvent(BuscarLibroEvent.OnClickBuscar) },
                selectedExtensions = selectedExtensions,
                onToggleExtension = { onBuscarLibroEvent(BuscarLibroEvent.OnToggleExtension(it)) },
                selectedLanguage = selectedLanguage,
                onIdiomaChange = { onBuscarLibroEvent(BuscarLibroEvent.OnIdiomaChange(it)) }
            )
        },
        content = { padding ->
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(400)) togetherWith fadeOut(
                        animationSpec = tween(
                            400
                        )
                    )
                },
                label = "UI State Transition",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) { targetState ->
                when (targetState) {
                    UIStateEnum.CARGANDO -> PantallaCarga()
                    UIStateEnum.CARGADO -> {
                        MostrarLibros(
                            libros = libros,
                            onLibroClick = onLibroClick,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }

                    UIStateEnum.ERROR -> {
                        ErrorScreen(
                            mensaje = "No se encontraron resultados o hubo un error",
                            onReintentar = { onBuscarLibroEvent(BuscarLibroEvent.OnClickBuscar) }
                        )
                    }

                    else -> PantallaInicial()
                }
            }
        }
    )
}
