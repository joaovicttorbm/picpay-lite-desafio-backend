package com.example.joaomatos.picpay_lite_desafio_backend.transation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

public record Transaction(
        @Id Long id,
        Long payee,
        Long payer,
        BigDecimal value,
        @CreatedDate LocalDateTime createdAt) {
    public Transaction {
        value = value.setScale(2);
    }
}
