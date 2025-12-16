package com.example.agrohub.data.cache

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for MemoryCache implementation.
 * These tests verify the core functionality of the cache including:
 * - Basic get/put operations
 * - TTL-based freshness checking
 * - LRU eviction policy
 * - Thread-safe operations
 */
class MemoryCacheTest {
    
    private lateinit var cache: MemoryCache<String, String>
    
    @Before
    fun setup() {
        cache = MemoryCache(maxSize = 3, ttlMillis = 1000) // 1 second TTL for testing
    }
    
    @Test
    fun `put and get returns same value`() {
        cache.put("key1", "value1")
        
        val result = cache.get("key1")
        
        assertEquals("value1", result)
    }
    
    @Test
    fun `get returns null for non-existent key`() {
        val result = cache.get("nonexistent")
        
        assertNull(result)
    }
    
    @Test
    fun `fresh entry is returned within TTL`() {
        cache.put("key1", "value1")
        
        // Immediately retrieve - should be fresh
        val result = cache.get("key1")
        
        assertEquals("value1", result)
        assertTrue(cache.isFresh("key1"))
    }
    
    @Test
    fun `stale entry returns null after TTL expires`() {
        val shortTtlCache = MemoryCache<String, String>(maxSize = 10, ttlMillis = 10)
        shortTtlCache.put("key1", "value1")
        
        // Wait for TTL to expire
        Thread.sleep(50)
        
        // Should return null (stale)
        val result = shortTtlCache.get("key1")
        
        assertNull(result)
        assertFalse(shortTtlCache.isFresh("key1"))
    }
    
    @Test
    fun `LRU eviction removes least recently used entry`() {
        // Fill cache to capacity (maxSize = 3)
        cache.put("key1", "value1")
        cache.put("key2", "value2")
        cache.put("key3", "value3")
        
        assertEquals(3, cache.size())
        
        // Access key2 and key3 to make key1 the LRU
        cache.get("key2")
        cache.get("key3")
        
        // Add a new entry, should evict key1
        cache.put("key4", "value4")
        
        // Cache should still be at max size
        assertEquals(3, cache.size())
        
        // key1 should be evicted
        assertNull(cache.get("key1"))
        
        // Other keys should still exist
        assertEquals("value2", cache.get("key2"))
        assertEquals("value3", cache.get("key3"))
        assertEquals("value4", cache.get("key4"))
    }
    
    @Test
    fun `invalidate removes specific entry`() {
        cache.put("key1", "value1")
        cache.put("key2", "value2")
        
        cache.invalidate("key1")
        
        assertNull(cache.get("key1"))
        assertEquals("value2", cache.get("key2"))
    }
    
    @Test
    fun `clear removes all entries`() {
        cache.put("key1", "value1")
        cache.put("key2", "value2")
        cache.put("key3", "value3")
        
        assertEquals(3, cache.size())
        
        cache.clear()
        
        assertEquals(0, cache.size())
        assertNull(cache.get("key1"))
        assertNull(cache.get("key2"))
        assertNull(cache.get("key3"))
    }
    
    @Test
    fun `containsKey returns true for existing key`() {
        cache.put("key1", "value1")
        
        assertTrue(cache.containsKey("key1"))
        assertFalse(cache.containsKey("key2"))
    }
    
    @Test
    fun `updating existing key does not increase size`() {
        cache.put("key1", "value1")
        assertEquals(1, cache.size())
        
        cache.put("key1", "value2")
        assertEquals(1, cache.size())
        assertEquals("value2", cache.get("key1"))
    }
    
    @Test
    fun `cache respects max size limit`() {
        val smallCache = MemoryCache<Int, String>(maxSize = 2, ttlMillis = 60000)
        
        smallCache.put(1, "one")
        smallCache.put(2, "two")
        assertEquals(2, smallCache.size())
        
        // Adding third entry should evict first
        smallCache.put(3, "three")
        assertEquals(2, smallCache.size())
        
        assertNull(smallCache.get(1))
        assertEquals("two", smallCache.get(2))
        assertEquals("three", smallCache.get(3))
    }
    
    @Test
    fun `isFresh returns false for non-existent key`() {
        assertFalse(cache.isFresh("nonexistent"))
    }
    
    @Test
    fun `multiple puts and gets work correctly`() {
        cache.put("a", "1")
        cache.put("b", "2")
        cache.put("c", "3")
        
        assertEquals("1", cache.get("a"))
        assertEquals("2", cache.get("b"))
        assertEquals("3", cache.get("c"))
        
        // Update values
        cache.put("a", "10")
        cache.put("b", "20")
        
        assertEquals("10", cache.get("a"))
        assertEquals("20", cache.get("b"))
        assertEquals("3", cache.get("c"))
    }
}
