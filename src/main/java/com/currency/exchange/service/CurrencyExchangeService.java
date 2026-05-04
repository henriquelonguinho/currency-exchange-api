package com.currency.exchange.service;

import com.currency.exchange.client.treasury.TreasuryReportingRatesExchangeClient;
import com.currency.exchange.client.treasury.dto.ExchangeRateData;
import com.currency.exchange.client.treasury.query.FiscalDataQuery;
import com.currency.exchange.client.treasury.query.FiscalDataQuery.FilterOperator;
import com.currency.exchange.client.treasury.query.FiscalDataQuery.SortDirection;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class CurrencyExchangeService {

    private final TreasuryReportingRatesExchangeClient exchangeClient;

    public CurrencyExchangeService(TreasuryReportingRatesExchangeClient exchangeClient) {
        this.exchangeClient = exchangeClient;
    }

    public ExchangeRateData getExchangeRateByCurrencyAndDate(String currency, LocalDate minimumDate, LocalDate maximumDate) {
        String dateFrom = minimumDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String dateTo = maximumDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

        FiscalDataQuery query = FiscalDataQuery.builder()
                .filter("country_currency_desc", FilterOperator.EQ, currency)
                .filter("record_date", FilterOperator.LTE, dateTo)
                .filter("record_date", FilterOperator.GTE, dateFrom)
                .sort("record_date", SortDirection.DESC)
                .fields("country_currency_desc", "exchange_rate", "record_date")
                .pageSize(1)
                .build();

        var response = exchangeClient.getExchangeRates(query);
        if (response.data() == null || response.data().isEmpty()) {
            return null;
        }
        return response.data().getFirst();
    }

    public BigDecimal convertCurrency(BigDecimal amount, BigDecimal exchangeRate) {
        return amount.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
    }
}
