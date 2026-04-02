package com.pmdm.annas.ui.features.buscarLibro

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.pmdm.annas.model.BuscarLibroUiState
import com.pmdm.annas.ui.features.buscarLibro.components.BuscarLibroScaffold

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BuscarLibroScreen(
    uiState: BuscarLibroUiState,
    onBuscarLibroEvent: (BuscarLibroEvent) -> Unit,
    onLibroClick: (com.pmdm.annas.model.Libro) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val listState = rememberLazyListState()
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