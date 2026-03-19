package com.pmdm.annas.ui.features.buscarLibro.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.pmdm.annas.model.Libro

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MostrarLibros(
    modifier: Modifier = Modifier,
    libros: List<Libro>,
    onLibroClick: (Libro) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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

@OptIn(ExperimentalSharedTransitionApi::class)
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
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp, pressedElevation = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .height(IntrinsicSize.Min)
            ) {
                Surface(
                    modifier = Modifier
                        .width(90.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .sharedElement(
                            rememberSharedContentState(key = "image-${libro.enlace}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        ),
                    tonalElevation = 2.dp
                ) {
                    AsyncImage(
                        model = libro.portada,
                        contentDescription = "Portada de ${libro.titulo}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.SpaceBetween
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
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InfoBadge(text = libro.idioma, color = MaterialTheme.colorScheme.primaryContainer)
                        InfoBadge(text = libro.formato.uppercase(), color = MaterialTheme.colorScheme.secondaryContainer)
                        
                        if (libro.tamano.isNotEmpty() && libro.tamano != "Desconocido") {
                            Text(
                                text = libro.tamano,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoBadge(text: String, color: Color) {
    if (text.isEmpty() || text == "Desconocido") return
    
    Surface(
        color = color,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.wrapContentSize()
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
