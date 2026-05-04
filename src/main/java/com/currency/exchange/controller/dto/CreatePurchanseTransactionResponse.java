package com.currency.exchange.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreatePurchanseTransactionResponse (
    String id,
    String description,
    LocalDate date,
    BigDecimal amount
) { }
