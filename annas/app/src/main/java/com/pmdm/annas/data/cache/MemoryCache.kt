package com.pmdm.annas.data.cache

import android.util.LruCache
import com.pmdm.annas.model.CacheEntry
import com.pmdm.annas.model.Libro
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MemoryCache {

    companion object {
        private const val CACHE_TTL = 5 * 60 * 1000L // 5 minutos
    }

    private val cacheLock = Mutex()

    private val searchCache = object : LruCache<String, CacheEntry<List<Libro>>>(200) {
        override fun sizeOf(key: String, value: CacheEntry<List<Libro>>): Int {
            return value.data.size
        }
    }

    private val detailsCache =
        object : LruCache<String, CacheEntry<Pair<String, List<String>>>>(400) {
            override fun sizeOf(key: String, value: CacheEntry<Pair<String, List<String>>>): Int {
                return value.data.second.size
            }
        }

    // ---------------- SEARCH ----------------

    suspend fun getSearch(key: String): List<Libro>? {
        return cacheLock.withLock {
            val entry = searchCache.get(key) ?: return null

            if (System.currentTimeMillis() - entry.timestamp < CACHE_TTL) {
                entry.data
            } else {
                searchCache.remove(key)
                null
            }
        }
    }

    suspend fun putSearch(key: String, data: List<Libro>) {
        cacheLock.withLock {
            searchCache.put(key, CacheEntry(data))
        }
    }

    // ---------------- DETAILS ----------------

    suspend fun getDetails(key: String): Pair<String, List<String>>? {
        return cacheLock.withLock {
            val entry = detailsCache.get(key) ?: return null

            if (System.currentTimeMillis() - entry.timestamp < CACHE_TTL) {
                entry.data
            } else {
                detailsCache.remove(key)
                null
            }
        }
    }

    suspend fun putDetails(key: String, data: Pair<String, List<String>>) {
        cacheLock.withLock {
            detailsCache.put(key, CacheEntry(data))
        }
    }
}