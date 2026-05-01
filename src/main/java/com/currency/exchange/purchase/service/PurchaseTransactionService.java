package com.currency.exchange.purchase.service;

import com.currency.exchange.purchase.model.PurchaseTransaction;
import com.currency.exchange.purchase.repository.PurchaseTransactionRepository;
import com.currency.exchange.purchase.web.dto.PurchaseTransactionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PurchaseTransactionService {

    private final PurchaseTransactionRepository purchaseTransactionRepository;

    @Autowired
    public PurchaseTransactionService(PurchaseTransactionRepository purchaseTransactionRepository) {
        this.purchaseTransactionRepository = purchaseTransactionRepository;
    }

    public void create(PurchaseTransactionRequest request) {
        PurchaseTransaction purchaseTransaction = PurchaseTransaction.builder()
                .description(request.getDescription())
                .date(request.getDate())
                .amount(formatAmount(request.getAmount()))
                .build();

        purchaseTransactionRepository.save(purchaseTransaction);
    }

    public BigDecimal formatAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
