package com.pmdm.annas.ui.features.buscarLibro.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.pmdm.annas.R

@Composable
fun PantallaInicial() {
    Box(
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = rememberLottieComposition(
                LottieCompositionSpec.RawRes(
                    R.raw.girl_with_books
                )
            ).value,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(200.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Busca un libro para comenzar \uD83D\uDCDA",
            fontSize = 18.sp,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
        )
    }
}