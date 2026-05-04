package com.currency.exchange.client.treasury.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class TreasuryApiConfig {

    @Value("${treasury.api.base-url}")
    private String baseUrl;

    @Value("${treasury.api.connect-timeout}")
    private Duration connectTimeout;

    @Value("${treasury.api.read-timeout}")
    private Duration readTimeout;

    @Bean
    public RestClient treasuryRestClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(treasuryClientHttpRequestFactory())
                .build();
    }

    private ClientHttpRequestFactory treasuryClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        return factory;
    }
}
