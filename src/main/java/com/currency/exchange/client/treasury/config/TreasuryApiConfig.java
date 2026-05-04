package com.currency.exchange.client.treasury.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class TreasuryApiConfig {

    @Value("${treasury.api.base-url}")
    private String baseUrl;

    @Bean
    public RestClient treasuryRestClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
