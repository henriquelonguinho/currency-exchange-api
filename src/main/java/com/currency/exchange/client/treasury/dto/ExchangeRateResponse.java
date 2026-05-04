package com.currency.exchange.client.treasury.dto;

import java.util.List;

public record ExchangeRateResponse(
        List<ExchangeRateData> data
) {}
