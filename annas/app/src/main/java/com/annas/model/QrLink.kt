package com.annas.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

@Immutable
data class QrLinkUi(
    val title: String,
    val label: String,
    val url: String,
    val icon: ImageVector,
    val accent: Color,
    val accentSoft: Color
)
