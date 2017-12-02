package io.keystash.services.cache;

import java.util.concurrent.TimeUnit;

public final class CacheBuilder<K, V> {

    private Long expiryTimeDuration;
    private TimeUnit expiryTimeUnit;

    private CacheBuilder() {}

    public static <K, V> CacheBuilder<K, V> createBuilder() {
        return new CacheBuilder<>();
    }

    public CacheBuilder<K, V> withExpiryTime(long expiryTimeDuration, TimeUnit expiryTimeUnit) {
        this.expiryTimeDuration = expiryTimeDuration;
        this.expiryTimeUnit = expiryTimeUnit;

        return this;
    }

    public <K1 extends K, V1 extends V> Cache<K1, V1> build() {
        // TODO if memcache is available, we will use that, otherwise in memory
        if (expiryTimeDuration != null && expiryTimeUnit != null) {
            return new InMemoryCache<>(expiryTimeDuration, expiryTimeUnit);
        } else {
            return new InMemoryCache<>();
        }
    }

}
