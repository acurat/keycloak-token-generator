package org.acurat.tokens.keycloak.config;

import lombok.RequiredArgsConstructor;
import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CacheManager {

    private final EmbeddedCacheManager cacheManager;

    public <K, V> Cache<K, V> getCache(String cacheName) {
        return cacheManager.getCache(cacheName, true);
    }

}
