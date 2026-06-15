package com.annas.model

import androidx.compose.runtime.Immutable

@Immutable
data class CacheEntry<T>(
    val data: T,
    val timestamp: Long = System.currentTimeMillis()
)