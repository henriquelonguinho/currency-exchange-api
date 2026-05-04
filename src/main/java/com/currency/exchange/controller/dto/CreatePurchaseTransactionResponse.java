package com.currency.exchange.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreatePurchaseTransactionResponse(
    String id,
    String description,
    LocalDate date,
    BigDecimal amount
) { }
