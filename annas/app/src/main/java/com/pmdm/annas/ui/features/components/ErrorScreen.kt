package com.pmdm.annas.ui.features.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
fun ErrorScreen(
    mensaje: String,
    onReintentar: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = mensaje, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(
            onClick = {
                onReintentar()
            }
        ) {
            Text(text = "Reintentar", fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        LottieAnimation(
            composition = rememberLottieComposition(
                LottieCompositionSpec.RawRes(
                    R.raw.error
                )
            ).value,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(200.dp)
        )
    }
}