package com.pmdm.annas.ui.features.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InfoBadge(text: String, color: Color) {
    if (text.isEmpty() || text == "Desconocido") return

    // Diseño optimizado para Android 16: Contraste inteligente y formas fluidas
    Surface(
        color = color,
        contentColor = contentColorFor(color), // Garantiza legibilidad automática en API 36
        shape = RoundedCornerShape(12.dp), // Esquinas más suaves para el estilo "Clean"
        modifier = Modifier.wrapContentSize(),
        tonalElevation = 2.dp 
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black, // Peso máximo para legibilidad en badges
            textAlign = TextAlign.Center,
            letterSpacing = 0.4.sp,
            lineHeight = 12.sp
        )
    }
}
