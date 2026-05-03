package com.currency.exchange.exchange.client;

import com.currency.exchange.exchange.client.dto.ExchangeRateResponse;
import com.currency.exchange.exchange.client.query.FiscalDataQuery;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TreasuryReportingRatesExchangeClient {

    private static final String RATES_OF_EXCHANGE_ENDPOINT =
            "/services/api/fiscal_service/v1/accounting/od/rates_of_exchange";

    private final RestClient treasuryRestClient;

    public TreasuryReportingRatesExchangeClient(RestClient treasuryRestClient) {
        this.treasuryRestClient = treasuryRestClient;
    }

    public ExchangeRateResponse getExchangeRates(FiscalDataQuery query) {
        return treasuryRestClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(RATES_OF_EXCHANGE_ENDPOINT);
                    query.applyTo().accept(uriBuilder);
                    return uriBuilder.build();
                })
                .retrieve()
                .body(ExchangeRateResponse.class);
    }
}
