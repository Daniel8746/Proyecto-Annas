package com.pmdm.annas.ui.features.buscarLibro

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.pmdm.annas.model.Libro
import com.pmdm.annas.ui.features.UIStateEnum
import com.pmdm.annas.ui.features.buscarLibro.components.Buscador
import com.pmdm.annas.ui.features.buscarLibro.components.MostrarLibros
import com.pmdm.annas.ui.features.buscarLibro.components.PantallaInicial
import com.pmdm.annas.ui.features.components.ErrorScreen
import com.pmdm.annas.ui.features.components.PantallaCarga

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BuscarLibroScreen(
    buscarNombre: String,
    uiState: UIStateEnum?,
    libros: List<Libro>,
    selectedExtensions: List<String>,
    selectedLanguage: String?,
    pagina: Int,
    onBuscarLibroEvent: (BuscarLibroEvent) -> Unit,
    onLibroClick: (Libro) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope
) {
    val listState = rememberLazyListState()

    // Lógica para detectar la dirección del scroll
    var previousIndex by remember { mutableIntStateOf(0) }
    var previousScrollOffset by remember { mutableIntStateOf(0) }

    val showSearchBar by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex == 0) {
                true
            } else {
                val isUp = if (listState.firstVisibleItemIndex < previousIndex) {
                    true
                } else if (listState.firstVisibleItemIndex > previousIndex) {
                    false
                } else {
                    listState.firstVisibleItemScrollOffset <= previousScrollOffset
                }
                previousIndex = listState.firstVisibleItemIndex
                previousScrollOffset = listState.firstVisibleItemScrollOffset
                isUp
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { _ ->
        // Usamos un Box para que el buscador flote sobre el contenido
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(400)) togetherWith fadeOut(
                        animationSpec = tween(400)
                    )
                },
                label = "UI State Transition",
                modifier = Modifier.fillMaxSize()
            ) { targetState ->
                when (targetState) {
                    UIStateEnum.CARGANDO -> PantallaCarga()
                    UIStateEnum.CARGADO -> {
                        MostrarLibros(
                            libros = libros,
                            onLibroClick = onLibroClick,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                            listState = listState
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

            // El buscador ahora flota arriba y no empuja el contenido
            AnimatedVisibility(
                visible = showSearchBar,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Buscador(
                    buscarNombre = buscarNombre,
                    onValueChange = { onBuscarLibroEvent(BuscarLibroEvent.OnBuscarChange(it)) },
                    onBuscar = { onBuscarLibroEvent(BuscarLibroEvent.OnClickBuscar) },
                    selectedExtensions = selectedExtensions,
                    onToggleExtension = { onBuscarLibroEvent(BuscarLibroEvent.OnToggleExtension(it)) },
                    selectedLanguage = selectedLanguage,
                    onIdiomaChange = { onBuscarLibroEvent(BuscarLibroEvent.OnIdiomaChange(it)) },
                    pagina = pagina,
                    onPaginaChange = { onBuscarLibroEvent(BuscarLibroEvent.OnPaginaChange(it)) }
                )
            }
        }
    }
}
