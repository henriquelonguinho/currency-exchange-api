package com.currency.exchange.purchase.web;

import com.currency.exchange.client.treasury.TreasuryReportingRatesExchangeClient;
import com.currency.exchange.client.treasury.dto.ExchangeRateData;
import com.currency.exchange.client.treasury.dto.ExchangeRateResponse;
import com.currency.exchange.client.treasury.query.FiscalDataQuery;
import com.currency.exchange.model.PurchaseTransaction;
import com.currency.exchange.repository.PurchaseTransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PurchaseTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PurchaseTransactionRepository repository;

    @MockitoBean
    private TreasuryReportingRatesExchangeClient exchangeClient;

    @Nested
    @DisplayName("POST /purchase-transaction")
    class CreatePurchaseTransaction {

        @Test
        @DisplayName("Should create a purchase transaction successfully")
        void shouldCreatePurchaseTransaction() throws Exception {
            String requestBody = """
                    {
                        "description": "Office supplies",
                        "date": "2026-04-15",
                        "amount": 49.99
                    }
                    """;

            mockMvc.perform(post("/purchase-transaction")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.description").value("Office supplies"))
                    .andExpect(jsonPath("$.date").value("2026-04-15"))
                    .andExpect(jsonPath("$.amount").value(49.99));
        }

        @Test
        @DisplayName("Should round amount to 2 decimal places")
        void shouldRoundAmountToTwoDecimalPlaces() throws Exception {
            String requestBody = """
                    {
                        "description": "Rounded purchase",
                        "date": "2026-04-15",
                        "amount": 123.456
                    }
                    """;

            mockMvc.perform(post("/purchase-transaction")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.amount").value(123.46));
        }

        @Test
        @DisplayName("Should return 400 when description is missing")
        void shouldReturn400WhenDescriptionMissing() throws Exception {
            String requestBody = """
                    {
                        "date": "2026-04-15",
                        "amount": 49.99
                    }
                    """;

            mockMvc.perform(post("/purchase-transaction")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details", hasItem("Description is required")));
        }

        @Test
        @DisplayName("Should return 400 when description exceeds 50 characters")
        void shouldReturn400WhenDescriptionExceeds50Characters() throws Exception {
            String longDescription = "A".repeat(51);
            String requestBody = """
                    {
                        "description": "%s",
                        "date": "2026-04-15",
                        "amount": 49.99
                    }
                    """.formatted(longDescription);

            mockMvc.perform(post("/purchase-transaction")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details", hasItem("Description must not exceed 50 characters")));
        }

        @Test
        @DisplayName("Should return 400 when date is missing")
        void shouldReturn400WhenDateMissing() throws Exception {
            String requestBody = """
                    {
                        "description": "No date",
                        "amount": 49.99
                    }
                    """;

            mockMvc.perform(post("/purchase-transaction")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details", hasItem("Date is required and must be in the format yyyy-MM-dd")));
        }

        @Test
        @DisplayName("Should return 400 when amount is missing")
        void shouldReturn400WhenAmountMissing() throws Exception {
            String requestBody = """
                    {
                        "description": "No amount",
                        "date": "2026-04-15"
                    }
                    """;

            mockMvc.perform(post("/purchase-transaction")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details", hasItem("Amount is required")));
        }

        @Test
        @DisplayName("Should return 400 when amount is negative")
        void shouldReturn400WhenAmountIsNegative() throws Exception {
            String requestBody = """
                    {
                        "description": "Negative amount",
                        "date": "2026-04-15",
                        "amount": -10.00
                    }
                    """;

            mockMvc.perform(post("/purchase-transaction")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details", hasItem("Amount must be positive")));
        }

        @Test
        @DisplayName("Should return 400 when amount is zero")
        void shouldReturn400WhenAmountIsZero() throws Exception {
            String requestBody = """
                    {
                        "description": "Zero amount",
                        "date": "2026-04-15",
                        "amount": 0
                    }
                    """;

            mockMvc.perform(post("/purchase-transaction")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details", hasItem("Amount must be positive")));
        }

        @Test
        @DisplayName("Should return 400 when date format is invalid")
        void shouldReturn400WhenDateFormatIsInvalid() throws Exception {
            String requestBody = """
                    {
                        "description": "Bad date",
                        "date": "15-04-2026",
                        "amount": 49.99
                    }
                    """;

            mockMvc.perform(post("/purchase-transaction")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when request body is empty")
        void shouldReturn400WhenRequestBodyIsEmpty() throws Exception {
            mockMvc.perform(post("/purchase-transaction")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details").isArray())
                    .andExpect(jsonPath("$.details", hasSize(greaterThanOrEqualTo(3))));
        }
    }

    @Nested
    @DisplayName("GET /purchase-transaction/{id}?currency=")
    class GetPurchaseTransactionWithConversion {

        @Test
        @DisplayName("Should return purchase transaction with currency conversion")
        void shouldReturnTransactionWithConversion() throws Exception {
            PurchaseTransaction saved = repository.save(PurchaseTransaction.builder()
                    .description("Test purchase")
                    .date(LocalDate.of(2026, 3, 15))
                    .amount(new BigDecimal("100.00"))
                    .build());

            ExchangeRateData rateData = new ExchangeRateData(
                    "Brazil-Real",
                    new BigDecimal("5.254"),
                    LocalDate.of(2026, 3, 31));
            ExchangeRateResponse apiResponse = new ExchangeRateResponse(List.of(rateData));

            when(exchangeClient.getExchangeRates(any(FiscalDataQuery.class)))
                    .thenReturn(apiResponse);

            mockMvc.perform(get("/purchase-transaction/{id}", saved.getId())
                            .param("currency", "Brazil-Real"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                    .andExpect(jsonPath("$.description").value("Test purchase"))
                    .andExpect(jsonPath("$.transaction_date").value("2026-03-15"))
                    .andExpect(jsonPath("$.usd_amount").value(100.00))
                    .andExpect(jsonPath("$.exchange_rate").value(5.254))
                    .andExpect(jsonPath("$.converted_amount").value(525.40));
        }

        @Test
        @DisplayName("Should return 400 when no exchange rate is found within 6 months")
        void shouldReturn400WhenNoExchangeRateFound() throws Exception {
            PurchaseTransaction saved = repository.save(PurchaseTransaction.builder()
                    .description("Old purchase")
                    .date(LocalDate.of(2026, 3, 15))
                    .amount(new BigDecimal("50.00"))
                    .build());

            ExchangeRateResponse emptyResponse = new ExchangeRateResponse(List.of());

            when(exchangeClient.getExchangeRates(any(FiscalDataQuery.class)))
                    .thenReturn(emptyResponse);

            mockMvc.perform(get("/purchase-transaction/{id}", saved.getId())
                            .param("currency", "Brazil-Real"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details", hasItem("The purchase cannot be converted to the target currency")));
        }

        @Test
        @DisplayName("Should return 400 when transaction is not found")
        void shouldReturn400WhenTransactionNotFound() throws Exception {
            String fakeId = "00000000-0000-0000-0000-000000000000";

            mockMvc.perform(get("/purchase-transaction/{id}", fakeId)
                            .param("currency", "Brazil-Real"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details", hasItem("Transaction not found")));
        }

        @Test
        @DisplayName("Should correctly convert with different exchange rates")
        void shouldCorrectlyConvertWithDifferentRates() throws Exception {
            PurchaseTransaction saved = repository.save(PurchaseTransaction.builder()
                    .description("Euro purchase")
                    .date(LocalDate.of(2026, 2, 10))
                    .amount(new BigDecimal("250.50"))
                    .build());

            ExchangeRateData rateData = new ExchangeRateData(
                    "Euro Zone-Euro",
                    new BigDecimal("0.921"),
                    LocalDate.of(2026, 1, 31));
            ExchangeRateResponse apiResponse = new ExchangeRateResponse(List.of(rateData));

            when(exchangeClient.getExchangeRates(any(FiscalDataQuery.class)))
                    .thenReturn(apiResponse);

            mockMvc.perform(get("/purchase-transaction/{id}", saved.getId())
                            .param("currency", "Euro Zone-Euro"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.exchange_rate").value(0.921))
                    .andExpect(jsonPath("$.converted_amount").value(230.71));
        }

        @Test
        @DisplayName("Should return 400 when currency parameter is missing")
        void shouldReturn400WhenCurrencyParamMissing() throws Exception {
            PurchaseTransaction saved = repository.save(PurchaseTransaction.builder()
                    .description("No currency")
                    .date(LocalDate.of(2026, 3, 15))
                    .amount(new BigDecimal("100.00"))
                    .build());

            mockMvc.perform(get("/purchase-transaction/{id}", saved.getId()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details", hasItem("Parameter 'currency' is required")));
        }
    }
}
