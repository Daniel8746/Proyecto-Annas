package com.pmdm.annas.ui.features.buscarLibro.components

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pmdm.annas.model.BuscarLibroUiState
import com.pmdm.annas.ui.features.UIStateEnum
import com.pmdm.annas.ui.features.buscarLibro.BuscarLibroEvent
import com.pmdm.annas.ui.features.components.ErrorScreen
import com.pmdm.annas.ui.features.components.PantallaCarga

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BuscarLibroScaffold(
    uiState: BuscarLibroUiState,
    showSearchBar: Boolean,
    listState: LazyListState,
    onBuscarLibroEvent: (BuscarLibroEvent) -> Unit,
    onLibroClick: (com.pmdm.annas.model.Libro) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    Scaffold(modifier = Modifier.fillMaxSize()) {
        when (uiState.uiStateEnum) {
            UIStateEnum.CARGANDO -> PantallaCarga(texto = "Buscando en los archivos...")
            UIStateEnum.CARGADO -> MostrarLibros(
                libros = uiState.libros,
                pagina = uiState.pagina,
                onPaginaChange = { onBuscarLibroEvent(BuscarLibroEvent.OnPaginaChange(it)) },
                onLibroClick = onLibroClick,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                listState = listState
            )

            UIStateEnum.ERROR -> ErrorScreen(
                mensaje = "No se encontraron resultados o hubo un error",
                onReintentar = { onBuscarLibroEvent(BuscarLibroEvent.OnClickBuscar) }
            )

            else -> {} // Pantalla inicial se maneja dentro de MostrarLibros si quieres
        }

        if (showSearchBar) {
            Buscador(
                buscarNombre = uiState.buscar,
                onValueChange = { onBuscarLibroEvent(BuscarLibroEvent.OnBuscarChange(it)) },
                onBuscar = { onBuscarLibroEvent(BuscarLibroEvent.OnClickBuscar) },
                selectedExtensions = uiState.selectedExtensions,
                onToggleExtension = { onBuscarLibroEvent(BuscarLibroEvent.OnToggleExtension(it)) },
                selectedLanguage = uiState.selectedLanguage,
                onIdiomaChange = { onBuscarLibroEvent(BuscarLibroEvent.OnIdiomaChange(it)) }
            )
        }
    }
}