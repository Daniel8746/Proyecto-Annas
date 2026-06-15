package com.annas.ui.features.generarQr

import androidx.compose.ui.graphics.Color
import com.annas.model.QrPalette
import kotlinx.collections.immutable.persistentListOf

val ScreenTop = Color(0xFFFFFEFC)
val ScreenWarm = Color(0xFFFFF7EA)
val Ink = Color(0xFF111827)
val MutedInk = Color(0xFF667085)
val QrInk = Color(0xFF0B1220)

val qrPalettes = persistentListOf(
    QrPalette(
        accent = Color(0xFF006D77),
        accentSoft = Color(0xFFDFF7F2),
        surfaceGlow = Color(0xFFEAF8F4)
    ),
    QrPalette(
        accent = Color(0xFFC2410C),
        accentSoft = Color(0xFFFFE7D6),
        surfaceGlow = Color(0xFFFFF7EA)
    ),
    QrPalette(
        accent = Color(0xFF5B5BD6),
        accentSoft = Color(0xFFECEBFF),
        surfaceGlow = Color(0xFFF5F0FF)
    ),
    QrPalette(
        accent = Color(0xFF2F6F4E),
        accentSoft = Color(0xFFE2F5E9),
        surfaceGlow = Color(0xFFEDF8F0)
    ),
    QrPalette(
        accent = Color(0xFFB4235A),
        accentSoft = Color(0xFFFFE4EE),
        surfaceGlow = Color(0xFFFFF0F5)
    ),
    QrPalette(
        accent = Color(0xFF255E7E),
        accentSoft = Color(0xFFE2F1F8),
        surfaceGlow = Color(0xFFEEF8FB)
    )
)
