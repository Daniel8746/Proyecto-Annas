package com.annas.ui.features.libro

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.annas.data.extensions.findActivity
import com.annas.data.extensions.vibrateClick
import com.annas.model.DownloadState
import com.annas.model.Libro
import com.annas.ui.features.UIStateEnum
import com.annas.ui.features.components.ErrorScreen
import com.annas.ui.features.components.PantallaCarga
import com.annas.ui.features.libro.components.MostrarLibro
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.CancellationException

@Stable
private class PredictiveBackUiState {
    var progress by mutableFloatStateOf(0f)
        private set

    var swipeEdge by mutableIntStateOf(0)
        private set

    fun update(progress: Float, swipeEdge: Int) {
        this.progress = progress
        this.swipeEdge = swipeEdge
    }

    fun resetProgress() {
        progress = 0f
    }
}

@Stable
private class DownloadLauncherState {
    var isWaitingForDownload by mutableStateOf(false)
        private set

    fun waitForPreparedDownload() {
        isWaitingForDownload = true
    }

    fun consumePreparedDownload() {
        isWaitingForDownload = false
    }
}

@Composable
fun LibroScreen(
    libro: Libro,
    descripcion: String,
    uiStateEnum: UIStateEnum?,
    tiempoEspera: Int,
    enlacesServidor: PersistentList<String>,
    downloadState: DownloadState,
    onLibroEvent: (LibroEvent) -> Unit,
    onNavigateBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val context = LocalContext.current.findActivity()
    val haptic = LocalHapticFeedback.current

    val predictiveBackState = remember { PredictiveBackUiState() }
    val downloadLauncherState = remember { DownloadLauncherState() }

    val createFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(
            downloadState.mimeType.ifEmpty { "application/octet-stream" }
        )
    ) { uri ->
        onLibroEvent(LibroEvent.DescargarLibro(uri))
    }

    LaunchedEffect(downloadState.url, downloadLauncherState.isWaitingForDownload) {
        if (downloadLauncherState.isWaitingForDownload && downloadState.url.isNotEmpty()) {
            try {
                createFileLauncher.launch(downloadState.fileName)
            } catch (_: Exception) {
                // Evitar crash si no hay actividad que maneje el intent
            }
            downloadLauncherState.consumePreparedDownload()
        }
    }

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        BackHandler {
            context.vibrateClick()
            onNavigateBack()
        }
    } else {
        PredictiveBackHandler { progress ->
            // Vibración durante el gesto
            var lastHapticStep = -1

            try {
                progress.collect { event ->
                    predictiveBackState.update(event.progress, event.swipeEdge)

                    val hapticStep = (event.progress * 10).toInt()
                    if (event.progress > 0.05f && hapticStep > lastHapticStep) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        lastHapticStep = hapticStep
                    }
                }

                context.vibrateClick()
                onNavigateBack()
            } catch (_: CancellationException) {
                predictiveBackState.resetProgress()
            }
        }
    }

    val animatedBackProgress by animateFloatAsState(
        targetValue = predictiveBackState.progress,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "PredictiveBackAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                val currentProgress = animatedBackProgress

                val scale = 1f - currentProgress * 0.12f
                scaleX = scale
                scaleY = scale
                rotationY =
                    if (predictiveBackState.swipeEdge == 0) currentProgress * 3f else -currentProgress * 3f
                val maxTranslation = 24.dp.toPx()
                translationX =
                    if (predictiveBackState.swipeEdge == 0) currentProgress * maxTranslation else -currentProgress * maxTranslation
                alpha = 1f - currentProgress * 0.2f
                shape = RoundedCornerShape((currentProgress * 32).dp)
                clip = currentProgress > 0
            }
    ) {
        when (uiStateEnum) {
            UIStateEnum.CARGANDO ->
                PantallaCarga(texto = "Preparando tu lectura...${if (tiempoEspera > 0) "\nPor favor espere $tiempoEspera para que comience la descarga" else ""}")

            UIStateEnum.CARGADO ->
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
                        downloadLauncherState.waitForPreparedDownload()
                        onLibroEvent(LibroEvent.PrepararDescarga(context, url))
                    },
                    onReintentar = { onLibroEvent(LibroEvent.ObtenerLinksServidor(libro.enlace)) },
                    enlaceKey = libro.enlace,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope
                )

            else -> ErrorScreen(
                mensaje = "Error al abrir el libro",
                onReintentar = { onLibroEvent(LibroEvent.ObtenerLinksServidor(libro.enlace)) }
            )
        }
    }
}
