package com.currency.exchange.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record PurchaseTransactionResponse(
        String id,
        String description,
        @JsonProperty("transaction_date") LocalDate transactionDate,
        @JsonProperty("usd_amount") BigDecimal usdAmount,
        @JsonProperty("exchange_rate") BigDecimal exchangeRate,
        @JsonProperty("converted_amount") BigDecimal convertedAmount
) { }
