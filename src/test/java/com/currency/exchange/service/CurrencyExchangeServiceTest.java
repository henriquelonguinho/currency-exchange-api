package com.currency.exchange.service;

import com.currency.exchange.client.treasury.TreasuryReportingRatesExchangeClient;
import com.currency.exchange.client.treasury.dto.ExchangeRateData;
import com.currency.exchange.client.treasury.dto.ExchangeRateResponse;
import com.currency.exchange.service.cache.ExchangeRateCacheKey;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyExchangeServiceTest {

    @Mock
    private TreasuryReportingRatesExchangeClient exchangeClient;

    private CurrencyExchangeService currencyExchangeService;

    @BeforeEach
    void setUp() {
        Cache<ExchangeRateCacheKey, ExchangeRateData> cache = Caffeine.newBuilder().build();
        currencyExchangeService = new CurrencyExchangeService(exchangeClient, cache);
    }

    @Nested
    @DisplayName("getExchangeRateByCurrencyAndDate")
    class GetExchangeRateByCurrencyAndDate {

        @Test
        @DisplayName("Should return exchange rate data when API returns results")
        void shouldReturnExchangeRateData() {
            ExchangeRateData rateData = new ExchangeRateData(
                    "Brazil-Real",
                    new BigDecimal("5.254"),
                    LocalDate.of(2026, 3, 31));
            ExchangeRateResponse response = new ExchangeRateResponse(List.of(rateData));

            when(exchangeClient.getExchangeRates(any())).thenReturn(response);

            ExchangeRateData result = currencyExchangeService.getExchangeRateByCurrencyAndDate(
                    "Brazil-Real",
                    LocalDate.of(2025, 9, 15),
                    LocalDate.of(2026, 3, 15));

            assertThat(result).isNotNull();
            assertThat(result.countryCurrencyDesc()).isEqualTo("Brazil-Real");
            assertThat(result.exchangeRate()).isEqualByComparingTo("5.254");
            assertThat(result.recordDate()).isEqualTo(LocalDate.of(2026, 3, 31));
        }

        @Test
        @DisplayName("Should return null when API returns empty data list")
        void shouldReturnNullWhenEmptyData() {
            ExchangeRateResponse response = new ExchangeRateResponse(Collections.emptyList());

            when(exchangeClient.getExchangeRates(any())).thenReturn(response);

            ExchangeRateData result = currencyExchangeService.getExchangeRateByCurrencyAndDate(
                    "Brazil-Real",
                    LocalDate.of(2025, 9, 15),
                    LocalDate.of(2026, 3, 15));

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null when API returns null data")
        void shouldReturnNullWhenNullData() {
            ExchangeRateResponse response = new ExchangeRateResponse(null);

            when(exchangeClient.getExchangeRates(any())).thenReturn(response);

            ExchangeRateData result = currencyExchangeService.getExchangeRateByCurrencyAndDate(
                    "Brazil-Real",
                    LocalDate.of(2025, 9, 15),
                    LocalDate.of(2026, 3, 15));

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return first element when API returns multiple results")
        void shouldReturnFirstElement() {
            ExchangeRateData first = new ExchangeRateData(
                    "Brazil-Real",
                    new BigDecimal("5.300"),
                    LocalDate.of(2026, 3, 31));
            ExchangeRateData second = new ExchangeRateData(
                    "Brazil-Real",
                    new BigDecimal("5.100"),
                    LocalDate.of(2026, 2, 28));
            ExchangeRateResponse response = new ExchangeRateResponse(List.of(first, second));

            when(exchangeClient.getExchangeRates(any())).thenReturn(response);

            ExchangeRateData result = currencyExchangeService.getExchangeRateByCurrencyAndDate(
                    "Brazil-Real",
                    LocalDate.of(2025, 9, 15),
                    LocalDate.of(2026, 3, 15));

            assertThat(result.exchangeRate()).isEqualByComparingTo("5.300");
        }
    }

    @Nested
    @DisplayName("convertCurrency")
    class ConvertCurrency {

        @Test
        @DisplayName("Should multiply amount by exchange rate and round to 2 decimal places")
        void shouldConvertCorrectly() {
            BigDecimal result = currencyExchangeService.convertCurrency(
                    new BigDecimal("100.00"),
                    new BigDecimal("5.254"));

            assertThat(result).isEqualByComparingTo("525.40");
        }

        @Test
        @DisplayName("Should round using HALF_UP")
        void shouldRoundHalfUp() {
            BigDecimal result = currencyExchangeService.convertCurrency(
                    new BigDecimal("33.33"),
                    new BigDecimal("3.333"));

            assertThat(result).isEqualByComparingTo("111.09");
        }

        @Test
        @DisplayName("Should handle exchange rate less than 1")
        void shouldHandleRateLessThanOne() {
            BigDecimal result = currencyExchangeService.convertCurrency(
                    new BigDecimal("250.50"),
                    new BigDecimal("0.921"));

            assertThat(result).isEqualByComparingTo("230.71");
        }

        @Test
        @DisplayName("Should return zero when amount is zero")
        void shouldReturnZeroWhenAmountIsZero() {
            BigDecimal result = currencyExchangeService.convertCurrency(
                    BigDecimal.ZERO,
                    new BigDecimal("5.254"));

            assertThat(result).isEqualByComparingTo("0.00");
        }
    }
}
