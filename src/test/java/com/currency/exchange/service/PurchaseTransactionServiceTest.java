package com.currency.exchange.service;

import com.currency.exchange.exception.custom.BusinessException;
import com.currency.exchange.exception.custom.CurrencyConversionException;
import com.currency.exchange.exception.custom.TransactionNotFoundException;
import com.currency.exchange.client.treasury.dto.ExchangeRateData;
import com.currency.exchange.model.PurchaseTransaction;
import com.currency.exchange.repository.PurchaseTransactionRepository;
import com.currency.exchange.dto.CreatePurchaseTransactionResponse;
import com.currency.exchange.dto.CreatePurchaseTransactionRequest;
import com.currency.exchange.dto.PurchaseTransactionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseTransactionServiceTest {

    @Mock
    private PurchaseTransactionRepository purchaseTransactionRepository;

    @Mock
    private CurrencyExchangeService currencyExchangeService;

    @InjectMocks
    private PurchaseTransactionService purchaseTransactionService;

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Should create a purchase transaction and return response")
        void shouldCreatePurchaseTransaction() {
            var request = CreatePurchaseTransactionRequest.builder()
                    .description("Office supplies")
                    .date(LocalDate.of(2026, 4, 15))
                    .amount(new BigDecimal("49.99"))
                    .build();

            UUID generatedId = UUID.randomUUID();
            PurchaseTransaction saved = PurchaseTransaction.builder()
                    .id(generatedId)
                    .description("Office supplies")
                    .date(LocalDate.of(2026, 4, 15))
                    .amount(new BigDecimal("49.99"))
                    .build();

            when(purchaseTransactionRepository.save(any(PurchaseTransaction.class)))
                    .thenReturn(saved);

            CreatePurchaseTransactionResponse response = purchaseTransactionService.create(request);

            assertThat(response.id()).isEqualTo(generatedId.toString());
            assertThat(response.description()).isEqualTo("Office supplies");
            assertThat(response.date()).isEqualTo(LocalDate.of(2026, 4, 15));
            assertThat(response.amount()).isEqualByComparingTo("49.99");
            verify(purchaseTransactionRepository).save(any(PurchaseTransaction.class));
        }

        @Test
        @DisplayName("Should round amount to 2 decimal places")
        void shouldRoundAmount() {
            var request = CreatePurchaseTransactionRequest.builder()
                    .description("Rounded")
                    .date(LocalDate.of(2026, 4, 15))
                    .amount(new BigDecimal("123.456"))
                    .build();

            PurchaseTransaction saved = PurchaseTransaction.builder()
                    .id(UUID.randomUUID())
                    .description("Rounded")
                    .date(LocalDate.of(2026, 4, 15))
                    .amount(new BigDecimal("123.46"))
                    .build();

            when(purchaseTransactionRepository.save(any(PurchaseTransaction.class)))
                    .thenReturn(saved);

            CreatePurchaseTransactionResponse response = purchaseTransactionService.create(request);

            assertThat(response.amount()).isEqualByComparingTo("123.46");
        }
    }

    @Nested
    @DisplayName("getPurchaseTransactionWithConversion")
    class GetPurchaseTransactionWithConversion {

        @Test
        @DisplayName("Should return transaction with converted amount")
        void shouldReturnTransactionWithConversion() {
            UUID id = UUID.randomUUID();
            PurchaseTransaction transaction = PurchaseTransaction.builder()
                    .id(id)
                    .description("Test purchase")
                    .date(LocalDate.of(2026, 3, 15))
                    .amount(new BigDecimal("100.00"))
                    .build();

            ExchangeRateData rateData = new ExchangeRateData(
                    "Brazil-Real",
                    new BigDecimal("5.254"),
                    LocalDate.of(2026, 3, 31));

            when(purchaseTransactionRepository.findById(id))
                    .thenReturn(Optional.of(transaction));
            when(currencyExchangeService.getExchangeRateByCurrencyAndDate(
                    eq("Brazil-Real"),
                    eq(LocalDate.of(2025, 9, 15)),
                    eq(LocalDate.of(2026, 3, 15))))
                    .thenReturn(rateData);
            when(currencyExchangeService.convertCurrency(
                    new BigDecimal("100.00"),
                    new BigDecimal("5.254")))
                    .thenReturn(new BigDecimal("525.40"));

            PurchaseTransactionResponse response =
                    purchaseTransactionService.getPurchaseTransactionWithConversion(id.toString(), "Brazil-Real");

            assertThat(response.id()).isEqualTo(id.toString());
            assertThat(response.description()).isEqualTo("Test purchase");
            assertThat(response.transactionDate()).isEqualTo(LocalDate.of(2026, 3, 15));
            assertThat(response.usdAmount()).isEqualByComparingTo("100.00");
            assertThat(response.exchangeRate()).isEqualByComparingTo("5.254");
            assertThat(response.convertedAmount()).isEqualByComparingTo("525.40");
        }

        @Test
        @DisplayName("Should throw CurrencyConversionException when no exchange rate found")
        void shouldThrowWhenNoExchangeRateFound() {
            UUID id = UUID.randomUUID();
            PurchaseTransaction transaction = PurchaseTransaction.builder()
                    .id(id)
                    .description("Old purchase")
                    .date(LocalDate.of(2026, 3, 15))
                    .amount(new BigDecimal("50.00"))
                    .build();

            when(purchaseTransactionRepository.findById(id))
                    .thenReturn(Optional.of(transaction));
            when(currencyExchangeService.getExchangeRateByCurrencyAndDate(
                    any(), any(), any()))
                    .thenReturn(null);

            assertThatThrownBy(() ->
                    purchaseTransactionService.getPurchaseTransactionWithConversion(id.toString(), "Brazil-Real"))
                    .isInstanceOf(CurrencyConversionException.class)
                    .hasMessage("The purchase cannot be converted to the target currency");

            verify(currencyExchangeService, never()).convertCurrency(any(), any());
        }

        @Test
        @DisplayName("Should throw TransactionNotFoundException when transaction does not exist")
        void shouldThrowWhenTransactionNotFound() {
            UUID id = UUID.randomUUID();

            when(purchaseTransactionRepository.findById(id))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    purchaseTransactionService.getPurchaseTransactionWithConversion(id.toString(), "Brazil-Real"))
                    .isInstanceOf(TransactionNotFoundException.class)
                    .hasMessage("Transaction not found");

            verifyNoInteractions(currencyExchangeService);
        }
    }

    @Nested
    @DisplayName("formatAmount")
    class FormatAmount {

        @Test
        @DisplayName("Should round to 2 decimal places using HALF_UP")
        void shouldRoundToTwoDecimalPlaces() {
            BigDecimal result = purchaseTransactionService.formatAmount(new BigDecimal("123.456"));
            assertThat(result).isEqualByComparingTo("123.46");
        }

        @Test
        @DisplayName("Should keep amount with exactly 2 decimal places")
        void shouldKeepTwoDecimalPlaces() {
            BigDecimal result = purchaseTransactionService.formatAmount(new BigDecimal("50.00"));
            assertThat(result).isEqualByComparingTo("50.00");
        }

        @Test
        @DisplayName("Should throw BusinessException when amount is null")
        void shouldThrowWhenAmountIsNull() {
            assertThatThrownBy(() -> purchaseTransactionService.formatAmount(null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Amount must be positive");
        }

        @Test
        @DisplayName("Should throw BusinessException when amount is negative")
        void shouldThrowWhenAmountIsNegative() {
            assertThatThrownBy(() -> purchaseTransactionService.formatAmount(new BigDecimal("-10.00")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Amount must be positive");
        }
    }
}
