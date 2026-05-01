package com.pmdm.annas.data.cache

data class CacheEntry<T>(
    val data: T,
    val timestamp: Long = System.currentTimeMillis()
)