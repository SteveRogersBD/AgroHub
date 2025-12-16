package com.example.agrohub.data.cache

/**
 * Generic in-memory cache with LRU (Least Recently Used) eviction policy and TTL (Time To Live) support.
 *
 * This cache is thread-safe and supports:
 * - LRU eviction when max capacity is reached
 * - TTL-based freshness checking
 * - Manual invalidation and clearing
 *
 * @param K The type of cache keys
 * @param V The type of cache values
 * @param maxSize Maximum number of entries in the cache (default: 100)
 * @param ttlMillis Time to live in milliseconds for cache entries (default: 5 minutes)
 */
class MemoryCache<K, V>(
    private val maxSize: Int = 100,
    private val ttlMillis: Long = 5 * 60 * 1000 // 5 minutes
) {
    /**
     * Internal data class to hold cached values with their timestamps
     */
    private data class CacheEntry<V>(
        val value: V,
        val timestamp: Long
    )

    /**
     * LinkedHashMap with access-order mode enabled for LRU behavior.
     * The third parameter (true) enables access-order, meaning entries are
     * reordered when accessed via get().
     */
    private val cache = LinkedHashMap<K, CacheEntry<V>>(maxSize, 0.75f, true)

    /**
     * Retrieves a value from the cache if it exists and is fresh.
     *
     * @param key The cache key
     * @return The cached value if it exists and is fresh, null otherwise
     */
    @Synchronized
    fun get(key: K): V? {
        val entry = cache[key] ?: return null
        
        // Check if entry is still fresh
        val currentTime = System.currentTimeMillis()
        return if (currentTime - entry.timestamp < ttlMillis) {
            // Entry is fresh, return the value
            entry.value
        } else {
            // Entry is stale, remove it and return null
            cache.remove(key)
            null
        }
    }

    /**
     * Stores a value in the cache with the current timestamp.
     * If the cache is at max capacity, the least recently used entry is evicted.
     *
     * @param key The cache key
     * @param value The value to cache
     */
    @Synchronized
    fun put(key: K, value: V) {
        // Check if we need to evict an entry
        if (cache.size >= maxSize && !cache.containsKey(key)) {
            // Remove the least recently used entry (first entry in LinkedHashMap)
            val firstKey = cache.keys.first()
            cache.remove(firstKey)
        }
        
        // Add or update the entry with current timestamp
        cache[key] = CacheEntry(value, System.currentTimeMillis())
    }

    /**
     * Removes a specific entry from the cache.
     *
     * @param key The cache key to invalidate
     */
    @Synchronized
    fun invalidate(key: K) {
        cache.remove(key)
    }

    /**
     * Clears all entries from the cache.
     */
    @Synchronized
    fun clear() {
        cache.clear()
    }

    /**
     * Returns the current number of entries in the cache.
     * Note: This includes both fresh and stale entries.
     *
     * @return The number of entries currently in the cache
     */
    @Synchronized
    fun size(): Int {
        return cache.size
    }

    /**
     * Checks if a key exists in the cache (regardless of freshness).
     *
     * @param key The cache key to check
     * @return true if the key exists in the cache, false otherwise
     */
    @Synchronized
    fun containsKey(key: K): Boolean {
        return cache.containsKey(key)
    }

    /**
     * Returns whether the cached entry for the given key is fresh.
     * Returns false if the key doesn't exist or the entry is stale.
     *
     * @param key The cache key to check
     * @return true if the entry exists and is fresh, false otherwise
     */
    @Synchronized
    fun isFresh(key: K): Boolean {
        val entry = cache[key] ?: return false
        val currentTime = System.currentTimeMillis()
        return currentTime - entry.timestamp < ttlMillis
    }
}
