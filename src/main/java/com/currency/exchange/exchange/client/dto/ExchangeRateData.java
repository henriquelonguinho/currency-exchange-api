package com.currency.exchange.exchange.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExchangeRateData(
        @JsonProperty("country_currency_desc") String countryCurrencyDesc,
        @JsonProperty("exchange_rate") BigDecimal exchangeRate,
        @JsonProperty("record_date") LocalDate recordDate
) {}
