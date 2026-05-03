package com.currency.exchange.exception.custom;

public class TransactionNotFoundException extends BusinessException{
    public TransactionNotFoundException(String message) {
        super(message);
    }
}
