package com.example.backend.application.service;

import com.example.backend.application.command.CheckoutCommand;
import com.example.backend.application.port.in.CheckoutUseCase;
import com.example.backend.application.test.PaymentDatabaseHelper;
import com.example.backend.application.test.PaymentTestConfiguration;
import com.example.backend.domain.PaymentEvent;
import com.example.backend.domain.PaymentOrder;
import com.example.backend.domain.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

@SpringBootTest
@Import(PaymentTestConfiguration.class)
class CheckoutServiceTest {

    private final CheckoutUseCase checkoutUseCase;
    private final PaymentDatabaseHelper paymentDatabaseHelper;

    public CheckoutServiceTest(
            @Autowired CheckoutUseCase checkoutUseCase,
            @Autowired PaymentDatabaseHelper paymentDatabaseHelper
    ) {
        this.checkoutUseCase = checkoutUseCase;
        this.paymentDatabaseHelper = paymentDatabaseHelper;
    }

    @BeforeEach
    void setUp() {
        paymentDatabaseHelper.clear();
    }

    @Test
    void checkout() {
        String orderId = UUID.randomUUID().toString();
        CheckoutCommand command = new CheckoutCommand(1L, List.of(1L, 2L, 3L), 1L, orderId);

        StepVerifier.create(checkoutUseCase.checkout(command))
                .expectNextMatches(result ->
                        result.getOrderId().equals(orderId)
                ).verifyComplete();


        PaymentEvent payment = paymentDatabaseHelper.getPayment(orderId);

        assert payment != null;
        assert payment.getPaymentOrders().size() == 3;
        assert payment.getPaymentOrders().stream().allMatch(paymentOrder -> paymentOrder.getPaymentStatus() == PaymentStatus.NOT_STARTED);
        assert !payment.isPaymentDone();
        assert payment.getPaymentOrders().stream().noneMatch(PaymentOrder::isLedgerUpdated);
        assert payment.getPaymentOrders().stream().noneMatch(PaymentOrder::isWalletUpdated);
    }
}