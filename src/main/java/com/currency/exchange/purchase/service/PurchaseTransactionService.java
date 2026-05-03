package com.currency.exchange.purchase.service;

import com.currency.exchange.exception.custom.BusinessException;
import com.currency.exchange.exception.custom.CurrencyConversionException;
import com.currency.exchange.exception.custom.TransactionNotFoundException;
import com.currency.exchange.exchange.service.CurrencyExchangeService;
import com.currency.exchange.purchase.model.PurchaseTransaction;
import com.currency.exchange.purchase.repository.PurchaseTransactionRepository;
import com.currency.exchange.purchase.web.dto.CreatePurchanseTransactionResponse;
import com.currency.exchange.purchase.web.dto.CreatePurchaseTransactionRequest;
import com.currency.exchange.purchase.web.dto.PurchaseTransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class PurchaseTransactionService {

    private final PurchaseTransactionRepository purchaseTransactionRepository;
    private final CurrencyExchangeService currencyExchangeService;

    @Autowired
    public PurchaseTransactionService(
            PurchaseTransactionRepository purchaseTransactionRepository,
            CurrencyExchangeService currencyExchangeService) {
        this.purchaseTransactionRepository = purchaseTransactionRepository;
        this.currencyExchangeService = currencyExchangeService;
    }

    public CreatePurchanseTransactionResponse create(CreatePurchaseTransactionRequest request) {
        PurchaseTransaction purchaseTransaction = PurchaseTransaction.builder()
                .description(request.getDescription())
                .date(request.getDate())
                .amount(formatAmount(request.getAmount()))
                .build();

        var savedTransaction = purchaseTransactionRepository.save(purchaseTransaction);
        return new CreatePurchanseTransactionResponse(
          savedTransaction.getId().toString(),
          savedTransaction.getDescription(),
          savedTransaction.getDate(),
          savedTransaction.getAmount()
        );
    }

    public PurchaseTransactionResponse getPurchaseTransactionWithConversion(String id, String currency) {
        PurchaseTransaction purchaseTransaction = getPuchaseTransaction(id);
        LocalDate minimumDate = purchaseTransaction.getDate().minusMonths(6);
        LocalDate maximumDate = purchaseTransaction.getDate();
        var exchangeData = currencyExchangeService.getExchangeRateByCurrencyAndDate(currency, minimumDate, maximumDate);
        if (exchangeData == null) {
            throw new CurrencyConversionException("The purchase cannot be converted to the target currency");
        }

        BigDecimal convertedAmount = currencyExchangeService.convertCurrency(purchaseTransaction.getAmount(), exchangeData.exchangeRate());
        return PurchaseTransactionResponse.builder()
                .id(purchaseTransaction.getId().toString())
                .description(purchaseTransaction.getDescription())
                .transactionDate(purchaseTransaction.getDate())
                .usdAmount(purchaseTransaction.getAmount())
                .exchangeRate(exchangeData.exchangeRate())
                .convertedAmount(convertedAmount)
                .build();
    }

    public PurchaseTransaction getPuchaseTransaction(String id) {
        return purchaseTransactionRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
    }

    public BigDecimal formatAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Amount must be positive");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
