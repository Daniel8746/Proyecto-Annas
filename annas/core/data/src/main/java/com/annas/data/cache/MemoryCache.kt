package com.annas.data.cache

import android.util.LruCache
import com.annas.model.CacheEntry
import com.annas.model.Libro
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MemoryCache {

    companion object {
        private const val CACHE_TTL = 5 * 60 * 1000L // 5 minutos
    }

    private val cacheLock = Mutex()

    private val searchCache = object : LruCache<String, CacheEntry<PersistentList<Libro>>>(80) {
        override fun sizeOf(key: String, value: CacheEntry<PersistentList<Libro>>): Int {
            return 1
        }
    }

    private val detailsCache =
        object : LruCache<String, CacheEntry<Pair<String, PersistentList<String>>>>(160) {
            override fun sizeOf(key: String, value: CacheEntry<Pair<String, PersistentList<String>>>): Int {
                return 1
            }
        }

    // ---------------- SEARCH ----------------

    suspend fun getSearch(key: String): PersistentList<Libro>? {
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

    suspend fun putSearch(key: String, data: PersistentList<Libro>) {
        cacheLock.withLock {
            searchCache.put(key, CacheEntry(data))
        }
    }

    // ---------------- DETAILS ----------------

    suspend fun getDetails(key: String): Pair<String, PersistentList<String>>? {
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

    suspend fun putDetails(key: String, data: Pair<String, PersistentList<String>>) {
        cacheLock.withLock {
            detailsCache.put(key, CacheEntry(data))
        }
    }
}
