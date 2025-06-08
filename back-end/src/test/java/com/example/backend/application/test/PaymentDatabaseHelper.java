package com.example.backend.application.test;

import com.example.backend.domain.PaymentEvent;

public interface  PaymentDatabaseHelper {

    PaymentEvent getPayment(String orderId);

    void clear();
}
