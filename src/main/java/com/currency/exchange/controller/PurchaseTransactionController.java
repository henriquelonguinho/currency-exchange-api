package com.currency.exchange.controller;

import com.currency.exchange.exception.ApiError;
import com.currency.exchange.service.PurchaseTransactionService;
import com.currency.exchange.dto.CreatePurchaseTransactionResponse;
import com.currency.exchange.dto.CreatePurchaseTransactionRequest;
import com.currency.exchange.dto.PurchaseTransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/purchase-transaction")
@Tag(name = "Purchase Transactions", description = "Store and retrieve purchase transactions with currency conversion")
public class PurchaseTransactionController {

    private final PurchaseTransactionService purchaseTransactionService;

    @Autowired
    public PurchaseTransactionController(PurchaseTransactionService purchaseTransactionService) {
        this.purchaseTransactionService = purchaseTransactionService;
    }

    @PostMapping
    @Operation(
            summary = "Create a purchase transaction",
            description = "Stores a new purchase transaction in US dollars. The amount is rounded to 2 decimal places.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Transaction created successfully"),
                    @ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(schema = @Schema(implementation = ApiError.class)))
            }
    )
    public ResponseEntity<CreatePurchaseTransactionResponse> create(@RequestBody @Valid CreatePurchaseTransactionRequest request) {
        CreatePurchaseTransactionResponse response = purchaseTransactionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Retrieve a transaction with currency conversion",
            description = "Retrieves a purchase transaction and converts the amount to the specified currency using Treasury exchange rates from the last 6 months.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transaction retrieved with conversion"),
                    @ApiResponse(responseCode = "404", description = "Transaction not found",
                            content = @Content(schema = @Schema(implementation = ApiError.class))),
                    @ApiResponse(responseCode = "422", description = "Currency conversion not possible",
                            content = @Content(schema = @Schema(implementation = ApiError.class))),
                    @ApiResponse(responseCode = "503", description = "Treasury API unavailable",
                            content = @Content(schema = @Schema(implementation = ApiError.class)))
            }
    )
    public ResponseEntity<PurchaseTransactionResponse> purchaseTransactionWithConversion(
            @Parameter(description = "UUID of the purchase transaction", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
            @PathVariable String id,
            @Parameter(description = "Target currency as listed by the Treasury API", example = "Brazil-Real")
            @RequestParam(name = "currency") String currency) {
        PurchaseTransactionResponse response = purchaseTransactionService.getPurchaseTransactionWithConversion(id, currency);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
