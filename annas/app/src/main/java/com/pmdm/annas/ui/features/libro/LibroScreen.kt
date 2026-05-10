package com.pmdm.annas.ui.features.libro

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.pmdm.annas.data.extensions.findActivity
import com.pmdm.annas.model.DownloadState
import com.pmdm.annas.model.Libro
import com.pmdm.annas.ui.features.UIStateEnum
import com.pmdm.annas.ui.features.components.ErrorScreen
import com.pmdm.annas.ui.features.components.PantallaCarga
import com.pmdm.annas.ui.features.libro.components.MostrarLibro
import kotlinx.coroutines.CancellationException

@SuppressLint("ServiceCast")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun LibroScreen(
    libro: Libro,
    descripcion: String,
    uiStateEnum: UIStateEnum?,
    enlacesServidor: List<String>,
    downloadState: DownloadState,
    onLibroEvent: (LibroEvent) -> Unit,
    onNavigateBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val context = LocalContext.current.findActivity()
    val haptic = LocalHapticFeedback.current

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

    var isWaitingForDownload by remember { mutableStateOf(false) }

    val createFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(
            downloadState.mimeType.ifEmpty { "application/octet-stream" }
        )
    ) { uri ->
        uri?.let { fileUri ->
            onLibroEvent(LibroEvent.DescargarLibro(fileUri))
        }
    }

    LaunchedEffect(downloadState.url) {
        if (isWaitingForDownload && downloadState.url.isNotEmpty()) {
            try {
                createFileLauncher.launch(downloadState.fileName)
            } catch (_: Exception) {
                // Evitar crash si no hay actividad que maneje el intent
            }
            isWaitingForDownload = false
        }
    }

    val triggerNativeVibration = {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        vibrator?.let { v ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else
                v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        BackHandler {
            triggerNativeVibration()
            onNavigateBack()
        }
    } else {
        PredictiveBackHandler { progress ->
            try {
                progress.collect { event ->
                    predictiveBackProgress = event.progress
                    swipeEdge = event.swipeEdge
                }
                triggerNativeVibration()
                onNavigateBack()
            } catch (_: CancellationException) {
                predictiveBackProgress = 0f
            }
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
        when (uiStateEnum) {
            UIStateEnum.CARGANDO ->
                PantallaCarga(texto = "Preparando tu lectura...")

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
                        isWaitingForDownload = true
                        onLibroEvent(LibroEvent.PrepararDescarga(context, url))
                    },
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