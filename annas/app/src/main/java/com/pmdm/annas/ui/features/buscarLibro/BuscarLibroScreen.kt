package com.pmdm.annas.ui.features.buscarLibro

import androidx.compose.animation.AnimatedContent
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

@Composable
fun BuscarLibroScreen(
    buscarNombre: String,
    uiState: UIStateEnum?,
    libros: List<Libro>,
    onBuscarLibroEvent: (BuscarLibroEvent) -> Unit,
    setLibroSeleccionado: (Libro) -> Unit,
    onNavigateToLibro: () -> Unit
) {
    Scaffold(
        topBar = {
            Buscador(
                buscarNombre = buscarNombre,
                onValueChange = { onBuscarLibroEvent(BuscarLibroEvent.OnBuscarChange(it)) },
                onBuscar = { onBuscarLibroEvent(BuscarLibroEvent.OnClickBuscar) }
            )
        },
        content = { padding ->
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    fadeIn(
                        animationSpec = tween(300)
                    ) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "Animated Content",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) { targetState ->
                when (targetState) {
                    UIStateEnum.CARGANDO -> PantallaCarga()
                    UIStateEnum.CARGADO -> {
                        MostrarLibros(
                            libros = libros,
                            setLibroSeleccionado = setLibroSeleccionado,
                            onClickLibro = {
                                onBuscarLibroEvent(
                                    BuscarLibroEvent.OnClickLibro(
                                        onNavigateToLibro
                                    )
                                )
                            }
                        )
                    }

                    UIStateEnum.ERROR -> {
                        ErrorScreen(
                            mensaje = "Error al buscar o cargar los libros",
                            onReintentar = { onBuscarLibroEvent(BuscarLibroEvent.OnClickBuscar) }
                        )
                    }

                    null -> PantallaInicial()
                }
            }
        }
    )
}