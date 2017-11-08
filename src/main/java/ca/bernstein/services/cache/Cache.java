package ca.bernstein.services.cache;

/**
 * A service that saves values of a given type to a cache, indexed by a given key
 */
public interface Cache<K, V> {

    /**
     * Sets a new value into cache indexed by a given key
     * @param key the lookup key to save the new entry under
     * @param value the value being saved to cache
     */
    void set(K key, V value);

    /**
     * Gets a value from the cache by a given key
     * @param key the key to lookup the value by
     * @return the found value, or null if not found
     */
    V get(K key);

    /**
     * Removes a value from the cache and returns it
     * @param key the key to lookup the value by
     * @return the found value that has been removed
     */
    V evict(K key);
}
