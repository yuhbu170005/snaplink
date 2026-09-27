package com.snaplink.config.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.snaplink.dto.internal.UrlRedirectDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class LocalCacheConfig {

    public static final int MAX_CACHE_SIZE = 10_000;
    public static final int EXPIRE_AFTER_WRITE_MINUTES = 5;

    @Bean
    public Cache<String, UrlRedirectDto> urlCaffeineCache() {
        return Caffeine.newBuilder()
                .maximumSize(MAX_CACHE_SIZE)
                .expireAfterWrite(EXPIRE_AFTER_WRITE_MINUTES, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }
}
