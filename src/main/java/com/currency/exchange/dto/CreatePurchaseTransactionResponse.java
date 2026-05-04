package com.currency.exchange.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreatePurchaseTransactionResponse(
    String id,
    String description,
    @JsonProperty("transaction_date") LocalDate date,
    BigDecimal amount
) { }
