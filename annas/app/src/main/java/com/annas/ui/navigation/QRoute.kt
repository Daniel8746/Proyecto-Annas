package com.annas.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.annas.ui.features.generarQr.GenerarQRScreen
import kotlinx.serialization.Serializable

@Serializable
object QrRoute

fun NavGraphBuilder.qrDestination(
    onNavigateBack: () -> Unit
) {
    composable<QrRoute> {
        GenerarQRScreen(onNavigateBack = onNavigateBack)
    }
}
