package com.example.joaomatos.picpay_lite_desafio_backend.transation;

public class InvalidTransactionException extends RuntimeException {
    public InvalidTransactionException(String message) {
        super(message);
    }
}
