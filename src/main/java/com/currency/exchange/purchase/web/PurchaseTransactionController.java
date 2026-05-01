package com.currency.exchange.purchase.web;

import com.currency.exchange.purchase.service.PurchaseTransactionService;
import com.currency.exchange.purchase.web.dto.PurchaseTransactionRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/purchase-transaction")
public class PurchaseTransactionController {

    private final PurchaseTransactionService purchaseTransactionService;

    @Autowired
    public PurchaseTransactionController(PurchaseTransactionService purchaseTransactionService) {
        this.purchaseTransactionService = purchaseTransactionService;
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid PurchaseTransactionRequest request) {
        purchaseTransactionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
