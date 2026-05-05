package com.currency.exchange.service.cache;

import java.time.LocalDate;

public record ExchangeRateCacheKey(
        String currency,
        LocalDate transactionDate
) {}
