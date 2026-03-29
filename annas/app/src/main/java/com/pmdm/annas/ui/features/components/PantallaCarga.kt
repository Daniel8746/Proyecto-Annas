package com.pmdm.annas.ui.features.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.pmdm.annas.R
import kotlinx.coroutines.launch

@Composable
fun PantallaCarga(texto: String = "Cargando...") {
    val compositionResult = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.book_search))
    val composition by compositionResult
    
    val lottieScale = remember { Animatable(0.92f) }
    val lottieTranslation = remember { Animatable(-15f) }
    val textAlpha = remember { Animatable(0.4f) }

    LaunchedEffect(Unit) {
        // Escala elástica del Lottie
        launch {
            while (true) {
                lottieScale.animateTo(1.08f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessVeryLow))
                lottieScale.animateTo(0.92f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessVeryLow))
            }
        }
        // Flotación física
        launch {
            while (true) {
                lottieTranslation.animateTo(15f, spring(stiffness = Spring.StiffnessVeryLow))
                lottieTranslation.animateTo(-15f, spring(stiffness = Spring.StiffnessVeryLow))
            }
        }
        // Respiración del texto
        launch {
            while (true) {
                textAlpha.animateTo(1f, spring(stiffness = Spring.StiffnessVeryLow))
                textAlpha.animateTo(0.4f, spring(stiffness = Spring.StiffnessVeryLow))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier
                .size(240.dp)
                .graphicsLayer {
                    scaleX = lottieScale.value
                    scaleY = lottieScale.value
                    translationY = lottieTranslation.value
                }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            modifier = Modifier.graphicsLayer { alpha = textAlpha.value }
        )
    }
}
