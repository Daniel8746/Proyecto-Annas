package com.annas.ui.features.buscarLibro

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.annas.model.BuscarLibroUiState
import com.annas.ui.features.buscarLibro.components.BuscarLibroScaffold

@Composable
fun BuscarLibroScreen(
    uiState: BuscarLibroUiState,
    onBuscarLibroEvent: (BuscarLibroEvent) -> Unit,
    onLibroClick: (com.annas.model.Libro) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val listState = rememberLazyListState()
    val showSearchBar by remember(listState) {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 || listState.lastScrolledBackward
        }
    }

    BuscarLibroScaffold(
        uiState = uiState,
        showSearchBar = showSearchBar,
        listState = listState,
        onBuscarLibroEvent = onBuscarLibroEvent,
        onLibroClick = onLibroClick,
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope
    )
}
