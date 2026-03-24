package com.pmdm.annas.ui.features.libro

import android.webkit.URLUtil
import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pmdm.annas.download.DownloadWebView
import com.pmdm.annas.download.NotificationHelper
import com.pmdm.annas.download.downloadFileWithNotification
import com.pmdm.annas.download.getMime
import com.pmdm.annas.model.Libro
import com.pmdm.annas.ui.features.UIStateEnum
import com.pmdm.annas.ui.features.components.ErrorScreen
import com.pmdm.annas.ui.features.components.PantallaCarga
import com.pmdm.annas.ui.features.libro.components.MostrarLibro
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun LibroScreen(
    libro: Libro,
    descripcion: String,
    uiStateEnum: UIStateEnum?,
    enlacesServidor: List<String>,
    onReintentar: () -> Unit,
    onNavigateBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    okHttpClient: OkHttpClient
) {
    var showWebView by remember { mutableStateOf(false) }
    var currentDownloadUrl by remember { mutableStateOf("") }
    var currentUserAgent by remember { mutableStateOf("") }
    var currentContentDisposition by remember { mutableStateOf("") }
    var currentMimeType by remember { mutableStateOf("application/octet-stream") }
    var currentFileName by remember { mutableStateOf("") }
    var currentLength by remember { mutableLongStateOf(0L) }
    var currentReferer by remember { mutableStateOf<String?>(null) }

    // Estado para la animación predictiva
    var predictiveBackProgress by remember { mutableFloatStateOf(0f) }
    var swipeEdge by remember { mutableIntStateOf(BackEventCompat.EDGE_LEFT) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val notificationHelper = remember { NotificationHelper(context) }

    val createFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        uri?.let {
            scope.launch {
                downloadFileWithNotification(
                    context = context,
                    client = okHttpClient,
                    url = currentDownloadUrl,
                    ua = currentUserAgent,
                    cd = currentContentDisposition,
                    mime = currentMimeType,
                    dest = it,
                    fileName = currentFileName,
                    helper = notificationHelper,
                    len = currentLength,
                    ref = currentReferer
                )
            }
        }
    }

    // Manejador del gesto lateral con animación completa
    PredictiveBackHandler(enabled = !showWebView) { progress ->
        try {
            progress.collect { event ->
                predictiveBackProgress = event.progress
                swipeEdge = event.swipeEdge
            }
            onNavigateBack()
        } catch (_: CancellationException) {
            predictiveBackProgress = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                // 1. Escala: Se encoge ligeramente (hasta un 10%)
                val scale = 1f - (predictiveBackProgress * 0.1f)
                scaleX = scale
                scaleY = scale
                
                // 2. Traslación: Se mueve lateralmente siguiendo el dedo (efecto pull)
                val maxTranslation = 30.dp.toPx()
                translationX = if (swipeEdge == BackEventCompat.EDGE_LEFT) {
                    predictiveBackProgress * maxTranslation
                } else {
                    -predictiveBackProgress * maxTranslation
                }
                
                // 3. Opacidad y Esquinas
                alpha = 1f - (predictiveBackProgress * 0.2f)
                shape = RoundedCornerShape((predictiveBackProgress * 28).dp)
                clip = predictiveBackProgress > 0
            }
    ) {
        when (uiStateEnum) {
            UIStateEnum.CARGANDO -> PantallaCarga()
            UIStateEnum.CARGADO -> MostrarLibro(
                portada = libro.portada, titulo = libro.titulo, autor = libro.autor,
                descripcion = descripcion, enlacesServidor = enlacesServidor,
                idioma = libro.idioma, formato = libro.formato, tamano = libro.tamano,
                onDownloadClick = { url ->
                    currentDownloadUrl = url
                    showWebView = true
                },
                enlaceKey = libro.enlace, sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope
            )

            else -> ErrorScreen(mensaje = "Error al abrir el libro", onReintentar = onReintentar)
        }

        if (showWebView) {
            ModalBottomSheet(
                onDismissRequest = { showWebView = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    DownloadWebView(
                        url = currentDownloadUrl,
                        onDownloadStart = { url, ua, cd, mime, len, ref ->
                            scope.launch {
                                currentDownloadUrl = url
                                currentUserAgent = ua
                                currentContentDisposition = cd
                                currentReferer = ref
                                currentLength = len

                                val suggestedMime =
                                    if (mime.isBlank() || mime == "application/octet-stream") {
                                        getMime(url)
                                    } else mime

                                currentMimeType = suggestedMime
                                val fileName = URLUtil.guessFileName(url, cd, suggestedMime)

                                currentFileName = fileName
                                showWebView = false
                                createFileLauncher.launch(currentFileName)
                            }
                        }
                    )
                    IconButton(
                        onClick = { showWebView = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.Close, "Cerrar")
                    }
                }
            }
        }
    }
}
