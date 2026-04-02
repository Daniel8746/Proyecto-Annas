package com.pmdm.annas.ui.features.libro

import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.pmdm.annas.download.NotificationHelper
import com.pmdm.annas.download.SilentDownloader
import com.pmdm.annas.model.Libro
import com.pmdm.annas.ui.features.UIStateEnum
import com.pmdm.annas.ui.features.buscarLibro.components.PantallaInicial
import com.pmdm.annas.ui.features.components.ErrorScreen
import com.pmdm.annas.ui.features.components.PantallaCarga
import com.pmdm.annas.ui.features.libro.components.MostrarLibro
import com.pmdm.annas.uri.UriUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

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
    silentDownloader: SilentDownloader
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val notificationHelper = remember { NotificationHelper(context) }

    // Estados para la descarga
    var downloadState by remember {
        mutableStateOf(
            DownloadState(
                url = "",
                userAgent = "",
                contentDisposition = "",
                mimeType = "application/octet-stream",
                fileName = "",
                length = 0L,
                referer = null
            )
        )
    }
    var isSearchingDownload by remember { mutableStateOf(false) }

    // Estados predictivos para swipe back
    var predictiveBackProgress by remember { mutableFloatStateOf(0f) }
    var swipeEdge by remember { mutableIntStateOf(0) }

    // Vibración durante el gesto
    LaunchedEffect(predictiveBackProgress) {
        if (predictiveBackProgress > 0.05f) {
            val tick = (predictiveBackProgress * 100).toInt()
            if (tick % 10 == 0) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val createFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        uri?.let { fileUri ->
            scope.launch {
                silentDownloader.downloadFileWithNotification(
                    url = downloadState.url,
                    ua = downloadState.userAgent,
                    cd = downloadState.contentDisposition,
                    mime = downloadState.mimeType,
                    dest = fileUri,
                    fileName = downloadState.fileName,
                    helper = notificationHelper,
                    len = downloadState.length,
                    ref = downloadState.referer
                )
            }
        }
    }

    PredictiveBackHandler(true) { progress ->
        try {
            progress.collect { event ->
                predictiveBackProgress = event.progress
                swipeEdge = event.swipeEdge
            }
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onNavigateBack()
        } catch (_: CancellationException) {
            predictiveBackProgress = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                val scale = 1f - predictiveBackProgress * 0.12f
                scaleX = scale
                scaleY = scale
                rotationY =
                    if (swipeEdge == 0) predictiveBackProgress * 3f else -predictiveBackProgress * 3f
                val maxTranslation = 24.dp.toPx()
                translationX =
                    if (swipeEdge == 0) predictiveBackProgress * maxTranslation else -predictiveBackProgress * maxTranslation
                alpha = 1f - predictiveBackProgress * 0.2f
                shape = RoundedCornerShape((predictiveBackProgress * 32).dp)
                clip = predictiveBackProgress > 0
            }
    ) {
        when {
            uiStateEnum == UIStateEnum.CARGANDO || isSearchingDownload ->
                PantallaCarga(texto = "Preparando tu lectura...")

            uiStateEnum == UIStateEnum.CARGADO ->
                MostrarLibro(
                    portada = libro.portada,
                    titulo = libro.titulo,
                    autor = libro.autor,
                    descripcion = descripcion,
                    enlacesServidor = enlacesServidor,
                    idioma = libro.idioma,
                    formato = libro.formato,
                    tamano = libro.tamano,
                    onDownloadClick = { url ->
                        isSearchingDownload = true
                        silentDownloader.launchSilentDownload(
                            url = url,
                            onDownloadStart = { dUrl, ua, cd, mime, len, ref ->
                                scope.launch {
                                    downloadState = DownloadState(
                                        url = dUrl,
                                        userAgent = ua,
                                        contentDisposition = cd,
                                        mimeType = if (mime.isBlank() || mime == "application/octet-stream") silentDownloader.getMime(
                                            dUrl
                                        ) else mime,
                                        fileName = UriUtils.decode(
                                            UriUtils.getRawFileName(
                                                dUrl,
                                                cd,
                                                mime
                                            )
                                        ),
                                        length = len,
                                        referer = ref
                                    )
                                    isSearchingDownload = false
                                    createFileLauncher.launch(downloadState.fileName)
                                }
                            }
                        )
                    },
                    enlaceKey = libro.enlace,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope
                )

            uiStateEnum == UIStateEnum.ERROR -> ErrorScreen(
                mensaje = "Error al abrir el libro",
                onReintentar = onReintentar
            )

            else -> PantallaInicial()
        }
    }
}

private data class DownloadState(
    val url: String,
    val userAgent: String,
    val contentDisposition: String,
    val mimeType: String,
    val fileName: String,
    val length: Long,
    val referer: String?
)