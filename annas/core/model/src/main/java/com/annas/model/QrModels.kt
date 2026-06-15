package com.annas.model

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.collections.immutable.PersistentList

@Immutable
data class QrPalette(
    val accent: Color,
    val accentSoft: Color,
    val surfaceGlow: Color
)

@Immutable
data class QrLinkUi(
    val title: String,
    val label: String,
    val url: String,
    val icon: ImageVector,
    val accent: Color,
    val accentSoft: Color
)

@Immutable
data class QrScreenContent(
    val links: PersistentList<QrLinkUi>,
    val backgroundColors: PersistentList<Color>
)
