package com.example.joaomatos.picpay_lite_desafio_backend.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.joaomatos.picpay_lite_desafio_backend.transation.Transaction;

public class NotificationService {
    private static Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);
    private NotificationProducer notificationProducer;

    public NotificationService(NotificationProducer notificationProducer) {
        this.notificationProducer = notificationProducer;
    }

    public void notify(Transaction transaction) {
        LOGGER.info("notifying transaction {}...", transaction);

        notificationProducer.SendNotificationProducer(transaction);
    }
}
