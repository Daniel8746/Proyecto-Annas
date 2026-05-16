package com.annas.ui.features.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import com.annas.R
import kotlin.math.roundToInt

@Composable
fun rememberBookCoverRequest(
    portada: String,
    width: Dp,
    height: Dp,
    cacheKey: String
): ImageRequest {
    val context = LocalContext.current
    val density = LocalDensity.current
    val widthPx = remember(width, density) {
        with(density) { width.toPx().roundToInt().coerceAtLeast(1) }
    }
    val heightPx = remember(height, density) {
        with(density) { height.toPx().roundToInt().coerceAtLeast(1) }
    }

    return remember(context, portada, widthPx, heightPx, cacheKey) {
        val data: Any = portada.ifBlank { R.drawable.pato_no_funciona }

        ImageRequest.Builder(context)
            .data(data)
            .size(widthPx, heightPx)
            .precision(Precision.INEXACT)
            .allowHardware(true)
            .crossfade(false)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .placeholder(R.drawable.pato_no_funciona)
            .error(R.drawable.pato_no_funciona)
            .fallback(R.drawable.pato_no_funciona)
            .apply {
                if (cacheKey.isNotBlank()) {
                    memoryCacheKey("cover-$cacheKey-$widthPx-$heightPx")
                }
                if (portada.isNotBlank()) {
                    diskCacheKey(portada)
                }
            }
            .build()
    }
}
