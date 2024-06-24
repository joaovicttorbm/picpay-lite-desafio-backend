package com.example.joaomatos.picpay_lite_desafio_backend.transation;

public class UnauthorizedTransactionException extends RuntimeException {
    public UnauthorizedTransactionException(String message) {
        super(message);
    }
}
