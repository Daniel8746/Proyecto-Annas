package com.annas.model

data class CacheEntry<T>(
    val data: T,
    val timestamp: Long = System.currentTimeMillis()
)