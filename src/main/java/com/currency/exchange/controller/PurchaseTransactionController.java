package com.currency.exchange.controller;

import com.currency.exchange.service.PurchaseTransactionService;
import com.currency.exchange.controller.dto.CreatePurchanseTransactionResponse;
import com.currency.exchange.controller.dto.CreatePurchaseTransactionRequest;
import com.currency.exchange.controller.dto.PurchaseTransactionResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/purchase-transaction")
public class PurchaseTransactionController {

    private final PurchaseTransactionService purchaseTransactionService;

    @Autowired
    public PurchaseTransactionController(PurchaseTransactionService purchaseTransactionService) {
        this.purchaseTransactionService = purchaseTransactionService;
    }

    @PostMapping
    public ResponseEntity<CreatePurchanseTransactionResponse> create(@RequestBody @Valid CreatePurchaseTransactionRequest request) {
        CreatePurchanseTransactionResponse response = purchaseTransactionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseTransactionResponse> purchaseTransactionWithConversion(
            @PathVariable String id,
            @RequestParam(name = "currency") String currency) {
        PurchaseTransactionResponse response = purchaseTransactionService.getPurchaseTransactionWithConversion(id, currency);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
