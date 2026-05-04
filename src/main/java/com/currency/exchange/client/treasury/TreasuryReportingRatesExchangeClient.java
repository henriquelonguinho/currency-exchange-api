package com.currency.exchange.client.treasury;

import com.currency.exchange.client.treasury.dto.ExchangeRateResponse;
import com.currency.exchange.client.treasury.query.FiscalDataQuery;
import com.currency.exchange.exception.custom.TreasuryApiClientException;
import com.currency.exchange.exception.custom.TreasuryApiUnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class TreasuryReportingRatesExchangeClient {

    private static final Logger log = LoggerFactory.getLogger(TreasuryReportingRatesExchangeClient.class);

    private static final String RATES_OF_EXCHANGE_ENDPOINT =
            "/v1/accounting/od/rates_of_exchange";

    private final RestClient treasuryRestClient;

    public TreasuryReportingRatesExchangeClient(RestClient treasuryRestClient) {
        this.treasuryRestClient = treasuryRestClient;
    }

    public ExchangeRateResponse getExchangeRates(FiscalDataQuery query) {
        try {
            return treasuryRestClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path(RATES_OF_EXCHANGE_ENDPOINT);
                        query.applyTo().accept(uriBuilder);
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .body(ExchangeRateResponse.class);
        } catch (ResourceAccessException ex) {
            log.error("Treasury API is unreachable (timeout/connection refused): {}", ex.getMessage());
            throw new TreasuryApiUnavailableException(
                    "Treasury exchange rate service is currently unavailable", ex);
        } catch (RestClientResponseException ex) {
            int status = ex.getStatusCode().value();
            if (status >= 400 && status < 500) {
                log.warn("Treasury API returned client error {}: {}", status, ex.getResponseBodyAsString());
                throw new TreasuryApiClientException(
                        "Treasury API rejected the request (HTTP %d)".formatted(status), ex);
            }
            log.error("Treasury API returned server error {}: {}", status, ex.getResponseBodyAsString());
            throw new TreasuryApiUnavailableException(
                    "Treasury exchange rate service returned an error (HTTP %d)".formatted(status), ex);
        }
    }
}
