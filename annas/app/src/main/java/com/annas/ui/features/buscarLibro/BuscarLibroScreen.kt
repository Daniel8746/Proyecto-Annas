package com.annas.ui.features.buscarLibro

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.annas.model.BuscarLibroUiState
import com.annas.ui.features.buscarLibro.components.BuscarLibroScaffold

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BuscarLibroScreen(
    uiState: BuscarLibroUiState,
    onBuscarLibroEvent: (BuscarLibroEvent) -> Unit,
    onLibroClick: (com.annas.model.Libro) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val listState = rememberLazyListState()
    var previousIndex by remember { mutableIntStateOf(0) }
    var previousScrollOffset by remember { mutableIntStateOf(0) }
    var showSearchBar by remember { mutableStateOf(true) }

    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            val shouldShowSearchBar = if (index == 0) {
                true
            } else {
                when {
                    index < previousIndex -> true
                    index > previousIndex -> false
                    else -> offset <= previousScrollOffset
                }
            }

            if (showSearchBar != shouldShowSearchBar) {
                showSearchBar = shouldShowSearchBar
            }

            previousIndex = index
            previousScrollOffset = offset
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
