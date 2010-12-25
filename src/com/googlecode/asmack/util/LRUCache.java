package com.googlecode.asmack.util;

import java.util.LinkedHashMap;

/**
 * A minimal LRU cache of fixed size, based on a LinkedHashMap.
 * @param <K> The key type.
 * @param <V> The value type.
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {

    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 1098273818193173881L;

    /**
     * Maximum number of cached elements.
     */
    private final int maxElements;

    /**
     * Create a new LRU cache with a maximum element count of maxElements.
     * @param maxElements The maximum number of elements.
     */
    public LRUCache(int maxElements) {
        super(maxElements * 2, 0.7f);
        this.maxElements = maxElements;
    }

    /**
     * Remove the oldest element when the cache is over capacity.
     * @param eldest The eldest entry. Ignored.
     */
    @Override
    protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
        return size() > maxElements;
    }

}
