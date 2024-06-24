package com.example.joaomatos.picpay_lite_desafio_backend.notification;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.joaomatos.picpay_lite_desafio_backend.transation.Transaction;

@Service

public class NotificationProducer {
    private final KafkaTemplate<String, Transaction> kafkaTemplate;

    public NotificationProducer(KafkaTemplate<String, Transaction> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void SendNotificationProducer(Transaction transaction) {
        kafkaTemplate.send("transaction-notification", transaction);
    }

}
