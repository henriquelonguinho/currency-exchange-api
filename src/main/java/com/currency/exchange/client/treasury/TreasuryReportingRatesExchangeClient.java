package com.currency.exchange.client.treasury;

import com.currency.exchange.client.treasury.dto.ExchangeRateResponse;
import com.currency.exchange.client.treasury.query.FiscalDataQuery;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TreasuryReportingRatesExchangeClient {

    private static final String RATES_OF_EXCHANGE_ENDPOINT =
            "/v1/accounting/od/rates_of_exchange";

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
