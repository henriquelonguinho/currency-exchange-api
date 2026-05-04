package com.currency.exchange.exception.custom;

public class TreasuryApiUnavailableException extends RuntimeException {

    public TreasuryApiUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
