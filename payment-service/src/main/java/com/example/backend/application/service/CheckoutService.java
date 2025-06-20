package com.example.backend.application.service;

import com.example.backend.application.command.CheckoutCommand;
import com.example.backend.application.port.in.CheckoutUseCase;
import com.example.backend.application.port.out.LoadProductPort;
import com.example.backend.application.port.out.SavePaymentPort;
import com.example.backend.common.UseCase;
import com.example.backend.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@UseCase
@Service
@RequiredArgsConstructor
public class CheckoutService implements CheckoutUseCase {

    private final LoadProductPort loadProductPort;
    private final SavePaymentPort savePaymentPort;

    @Override
    public Mono<CheckoutResult> checkout(CheckoutCommand command) {
        return loadProductPort.getProducts(command.cartId(), command.productIds())
                .collectList()
                .map(products -> createPaymentEvent(command, products))
                .flatMap(paymentEvent -> savePaymentPort.save(paymentEvent).thenReturn(paymentEvent))
                .map(paymentEvent ->
                        new CheckoutResult(paymentEvent.totalAmount(),
                                           paymentEvent.getOrderId(),
                                           paymentEvent.getOrderName()));
    }

    private PaymentEvent createPaymentEvent(CheckoutCommand command, List<Product> products) {
        return PaymentEvent.builder()
                .buyerId(command.buyerId())
                .orderId(command.idempotencyKey())
                .orderName(products.stream().map(Product::getName).collect(Collectors.joining(",")))
                .paymentOrders(products.stream()
                        .map(product -> PaymentOrder.builder()
                                .sellerId(product.getSellerId())
                                .orderId(command.idempotencyKey())
                                .productId(product.getId())
                                .amount(product.getAmount())
                                .paymentStatus(PaymentStatus.NOT_STARTED)
                                .build())
                        .toList())
                .build();
    }
}
