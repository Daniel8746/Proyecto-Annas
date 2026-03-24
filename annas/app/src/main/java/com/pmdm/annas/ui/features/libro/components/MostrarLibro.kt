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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
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
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.5f
                    )
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top // Cambiado a Top para soportar múltiples líneas de badges
                ) {
                    AsyncImage(
                        model = portada.ifBlank { R.drawable.pato_no_funciona },
                        contentDescription = "Portada de $titulo",
                        modifier = Modifier
                            .width(100.dp)
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .sharedElement(
                                rememberSharedContentState(key = "image-$enlaceKey"),
                                animatedVisibilityScope = animatedVisibilityScope
                            ),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.pato_no_funciona),
                        error = painterResource(id = R.drawable.pato_no_funciona)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = titulo,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 3,
                            modifier = Modifier.sharedElement(
                                rememberSharedContentState(key = "title-$enlaceKey"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        )
                        Text(
                            text = autor,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Cambiado a FlowRow para manejar textos largos (como el tamaño en EPUBs)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InfoBadge(
                                text = idioma,
                                color = MaterialTheme.colorScheme.primaryContainer
                            )
                            InfoBadge(
                                text = formato.uppercase(),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            )
                            InfoBadge(
                                text = tamano,
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle(icon = Icons.Default.Info, title = "Descripción")

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
                tonalElevation = 1.dp
            ) {
                ReadMoreText(
                    text = descripcion.ifBlank { "No hay descripción disponible para este libro." },
                    expanded = expanded,
                    onExpandedChange = onExpandedChange,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    // Leer más
                    readMoreText = "Leer más...",
                    readMoreColor = MaterialTheme.colorScheme.secondaryContainer,
                    readMoreFontWeight = FontWeight.Bold,
                    readMoreMaxLines = 5,
                    readMoreOverflow = ReadMoreTextOverflow.Ellipsis,

                    // Leer menos
                    readLessText = "Leer menos...",
                    readLessColor = MaterialTheme.colorScheme.secondaryContainer,
                    readLessFontWeight = FontWeight.Bold,

                    toggleArea = ToggleArea.All //ToggleArea.More
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(text = "Servidor ${index + 1}", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionTitle(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
