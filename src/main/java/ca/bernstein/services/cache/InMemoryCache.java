package ca.bernstein.services.cache;
import com.google.common.cache.CacheBuilder;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class InMemoryCache<K, V> implements Cache<K, V> {

    private static final TimeUnit DEFAULT_EXPIRY_TIME_UNIT = TimeUnit.HOURS;
    private static final long DEFAULT_EXPIRY_TIME_DURATION = 1;

    private final com.google.common.cache.Cache<CacheKey, CacheValue> cache;

    protected InMemoryCache() {
        this(DEFAULT_EXPIRY_TIME_DURATION, DEFAULT_EXPIRY_TIME_UNIT);
    }

    protected InMemoryCache(long expiryTimeDuration, TimeUnit expiryTimeUnit) {
        this.cache = CacheBuilder.newBuilder()
            .expireAfterWrite(expiryTimeDuration, expiryTimeUnit)
            .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void set(K key, V value) {
        cache.put(new CacheKey(key), new CacheValue(value));
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(K key) {
        CacheValue cacheValue = cache.getIfPresent(new CacheKey(key));
        return cacheValue != null ? (V) cacheValue.getValue() : null;
    }

    @Override
    public boolean has(K key) {
        return get(key) != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V evict(K key) {
        CacheKey cacheKey = new CacheKey(key);
        CacheValue cacheValue = cache.getIfPresent(cacheKey);
        cache.invalidate(cacheKey);
        return cacheValue != null ? (V) cacheValue.getValue() : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<V> values() {
        return (List<V>) cache.asMap().values().stream()
                .map(CacheValue::getValue)
                .collect(Collectors.toList());
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    private final class CacheKey {
        @Getter @Setter private Object value;
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    private final class CacheValue {
        @Getter @Setter private Object value;
    }
}
