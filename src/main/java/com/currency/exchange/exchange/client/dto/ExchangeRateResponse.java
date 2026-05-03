package com.currency.exchange.exchange.client.dto;

import java.util.List;

public record ExchangeRateResponse(
        List<ExchangeRateData> data
) {}
