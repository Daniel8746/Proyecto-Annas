package com.pmdm.annas.ui.features.buscarLibro.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pmdm.annas.R
import com.pmdm.annas.model.Libro
import com.pmdm.annas.ui.features.components.InfoBadge

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MostrarLibros(
    modifier: Modifier = Modifier,
    libros: List<Libro>,
    pagina: Int,
    onPaginaChange: (Int) -> Unit,
    onLibroClick: (Libro) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    listState: LazyListState
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Spacer(modifier = Modifier.height(92.dp)) } // Espacio top

        items(libros, key = { it.enlace }) { libro ->
            AnimatedLibroItem(
                libro = libro,
                onClick = { onLibroClick(libro) },
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope
            )
        }

        item { Paginacion(pagina = pagina, onPaginaChange = onPaginaChange) }
    }
}

@Composable
fun Paginacion(
    pagina: Int,
    onPaginaChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 64.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Física de muelles refinada para navegación de páginas (Butter Smooth)
        val springSpec = spring<Float>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        )

        FilledTonalIconButton(
            onClick = { if (pagina > 1) onPaginaChange(pagina - 1) },
            enabled = pagina > 1,
            modifier = Modifier.size(56.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                contentDescription = "Página anterior",
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(28.dp))

        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 4.dp
        ) {
            AnimatedContent(
                targetState = pagina,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInVertically(spring()) { height -> height / 2 } + fadeIn(springSpec)).togetherWith(
                            slideOutVertically(spring()) { height -> -height / 2 } + fadeOut(
                                springSpec
                            ))
                    } else {
                        (slideInVertically(spring()) { height -> -height / 2 } + fadeIn(springSpec)).togetherWith(
                            slideOutVertically(spring()) { height -> height / 2 } + fadeOut(
                                springSpec
                            ))
                    }.using(
                        SizeTransform(clip = false)
                    )
                },
                label = "PageNumberAnimation"
            ) { targetPage ->
                Text(
                    text = targetPage.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(28.dp))

        FilledTonalIconButton(
            onClick = { onPaginaChange(pagina + 1) },
            modifier = Modifier.size(56.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Siguiente página",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AnimatedLibroItem(
    libro: Libro,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    // Cascada orgánica para Android 16: Entrada en diagonal con inercia elástica
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = spring(stiffness = Spring.StiffnessVeryLow)
        ) + slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            ),
            initialOffsetY = { 80 }
        ) + slideInHorizontally(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            ),
            initialOffsetX = { 40 }
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
                .widthIn(max = 700.dp)
                .fillMaxWidth()
                .clickable { onClick() },
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 0.dp,
                pressedElevation = 8.dp,
                hoveredElevation = 4.dp
            ),
            shape = RoundedCornerShape(28.dp), // Redondeado más profundo y moderno
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .width(100.dp)
                        .aspectRatio(0.68f)
                        .clip(RoundedCornerShape(16.dp))
                        .sharedElement(
                            rememberSharedContentState(key = "image-${libro.enlace}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        ),
                    tonalElevation = 6.dp,
                    shadowElevation = 4.dp
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

                Spacer(modifier = Modifier.width(22.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(
                        text = libro.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.sharedElement(
                            rememberSharedContentState(key = "title-${libro.enlace}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        ),
                        letterSpacing = (-0.4).sp,
                        lineHeight = 22.sp
                    )

                    Text(
                        text = libro.autor,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        InfoBadge(
                            text = libro.idioma,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        )
                        InfoBadge(
                            text = libro.formato.uppercase(),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    }
                }
            }
        }
    }
}
