package com.pmdm.annas.ui.features.libro.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pmdm.annas.R
import com.pmdm.annas.ui.features.components.InfoBadge
import com.webtoonscorp.android.readmore.foundation.ReadMoreTextOverflow
import com.webtoonscorp.android.readmore.foundation.ToggleArea
import com.webtoonscorp.android.readmore.material3.ReadMoreText

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class)
@Composable
fun MostrarLibro(
    portada: String,
    titulo: String,
    autor: String,
    descripcion: String,
    enlacesServidor: List<String>,
    idioma: String,
    formato: String,
    tamano: String,
    onDownloadClick: (String) -> Unit,
    enlaceKey: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val (expanded, onExpandedChange) = rememberSaveable { mutableStateOf(false) }
    val statusBarsPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    with(sharedTransitionScope) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = statusBarsPadding + 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 8.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    AsyncImage(
                        model = portada.ifBlank { R.drawable.pato_no_funciona },
                        contentDescription = "Portada de $titulo",
                        modifier = Modifier
                            .width(110.dp)
                            .height(165.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .sharedElement(
                                rememberSharedContentState(key = "image-$enlaceKey"),
                                animatedVisibilityScope = animatedVisibilityScope
                            ),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.pato_no_funciona),
                        error = painterResource(id = R.drawable.pato_no_funciona)
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = titulo,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 3,
                            modifier = Modifier.sharedElement(
                                rememberSharedContentState(key = "title-$enlaceKey"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        )
                        Text(
                            text = autor,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InfoBadge(
                                text = idioma,
                                color = MaterialTheme.colorScheme.secondaryContainer
                            )
                            InfoBadge(
                                text = formato.uppercase(),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            )
                            InfoBadge(
                                text = tamano,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(modifier = Modifier.widthIn(max = 600.dp)) {
                SectionTitle(icon = Icons.Default.Info, title = "Descripción")

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 1.dp
                ) {
                    ReadMoreText(
                        text = descripcion.ifBlank { "No hay descripción disponible para este libro." },
                        expanded = expanded,
                        onExpandedChange = onExpandedChange,
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 24.sp,
                        readMoreText = "Leer más",
                        readMoreColor = MaterialTheme.colorScheme.primary,
                        readMoreFontWeight = FontWeight.Bold,
                        readMoreMaxLines = 5,
                        readMoreOverflow = ReadMoreTextOverflow.Ellipsis,
                        readLessText = "Leer menos",
                        readLessColor = MaterialTheme.colorScheme.primary,
                        readLessFontWeight = FontWeight.Bold,
                        toggleArea = ToggleArea.All
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                SectionTitle(icon = Icons.Default.Download, title = "Servidores de descarga")

                if (enlacesServidor.isEmpty()) {
                    Text(
                        text = "Buscando enlaces de descarga...",
                        modifier = Modifier.padding(vertical = 16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    enlacesServidor.forEachIndexed { index, enlaceServer ->
                        ElevatedButton(
                            onClick = { onDownloadClick(enlaceServer) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(text = "Servidor ${index + 1}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun SectionTitle(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp, start = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
    }
}
