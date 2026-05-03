package com.currency.exchange.purchase.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreatePurchanseTransactionResponse (
    String id,
    String description,
    LocalDate date,
    BigDecimal amount
) { }
