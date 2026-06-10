package com.annas.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class QrPalette(
    val accent: Color,
    val accentSoft: Color,
    val surfaceGlow: Color
)
