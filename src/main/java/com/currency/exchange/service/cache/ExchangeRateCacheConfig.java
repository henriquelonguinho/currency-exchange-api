package com.currency.exchange.service.cache;

import com.currency.exchange.client.treasury.dto.ExchangeRateData;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ExchangeRateCacheConfig {

    @Value("${treasury.api.cache.ttl}")
    private Duration ttl;

    @Value("${treasury.api.cache.max-size}")
    private int maxSize;

    @Bean
    public Cache<ExchangeRateCacheKey, ExchangeRateData> exchangeRateCache() {
        return Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(ttl)
                .build();
    }
}
