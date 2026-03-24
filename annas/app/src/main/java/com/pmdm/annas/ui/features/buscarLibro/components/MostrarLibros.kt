package com.pmdm.annas.ui.features.buscarLibro.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.pmdm.annas.R
import com.pmdm.annas.model.Libro
import com.pmdm.annas.ui.features.components.InfoBadge

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MostrarLibros(
    modifier: Modifier = Modifier,
    libros: List<Libro>,
    onLibroClick: (Libro) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    listState: LazyListState = rememberLazyListState()
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Espaciador para que el primer elemento no quede debajo del buscador flotante
        item {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(76.dp) // Altura aproximada del buscador (56 campo + 16/2 paddings)
            )
        }

        itemsIndexed(libros, key = { _, libro -> libro.enlace }) { index, libro ->
            AnimatedLibroItem(
                libro = libro,
                index = index,
                onClick = { onLibroClick(libro) },
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AnimatedLibroItem(
    libro: Libro,
    index: Int,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(durationMillis = 400, delayMillis = index * 50)
        ) + slideInVertically(
            animationSpec = tween(durationMillis = 400, delayMillis = index * 50),
            initialOffsetY = { 40 }
        )
    ) {
        LibroItem(
            libro = libro,
            onClick = onClick,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class)
@Composable
fun LibroItem(
    libro: Libro,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    with(sharedTransitionScope) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp
            ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Top // Alineamos al principio para que crezca bien
            ) {
                Surface(
                    modifier = Modifier
                        .width(90.dp)
                        .aspectRatio(2f / 3f) // Relación de aspecto de portada estándar
                        .clip(RoundedCornerShape(12.dp))
                        .sharedElement(
                            rememberSharedContentState(key = "image-${libro.enlace}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        ),
                    tonalElevation = 2.dp
                ) {
                    AsyncImage(
                        model = libro.portada.ifBlank { R.drawable.pato_no_funciona },
                        contentDescription = "Portada de ${libro.titulo}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.pato_no_funciona),
                        error = painterResource(id = R.drawable.pato_no_funciona)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 4.dp)
                ) {
                    Column {
                        Text(
                            text = libro.titulo,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.sharedElement(
                                rememberSharedContentState(key = "title-${libro.enlace}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = libro.autor,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Usamos FlowRow para que los badges bajen de línea si no caben
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoBadge(
                            text = libro.idioma,
                            color = MaterialTheme.colorScheme.primaryContainer
                        )
                        InfoBadge(
                            text = libro.formato,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        )
                        InfoBadge(
                            text = libro.tamano,
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    }
                }
            }
        }
    }
}
