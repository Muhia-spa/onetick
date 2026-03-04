package com.onetick.oidc;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OidcStateStore {
    private static final String CACHE_NAME = "oidcState";

    private final CacheManager cacheManager;
    private final Map<String, Long> fallback = new ConcurrentHashMap<>();
    private final OidcProperties properties;

    public OidcStateStore(CacheManager cacheManager, OidcProperties properties) {
        this.cacheManager = cacheManager;
        this.properties = properties;
    }

    public void store(String state) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.put(state, Boolean.TRUE);
        } else {
            fallback.put(state, Instant.now().toEpochMilli());
        }
    }

    public boolean consume(String state) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            Boolean value = cache.get(state, Boolean.class);
            cache.evict(state);
            return Boolean.TRUE.equals(value);
        }
        Long createdAt = fallback.remove(state);
        if (createdAt == null) {
            return false;
        }
        return Instant.now().toEpochMilli() - createdAt <= properties.getStateTtlMs();
    }
}
