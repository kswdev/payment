package com.example.backend.application.command;

import com.example.backend.domain.PaymentExecutionResult;
import com.example.backend.domain.PaymentFailure;
import com.example.backend.domain.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PaymentStatusUpdateCommand {
    private String paymentKey;
    private String orderId;
    private PaymentStatus status;
    private PaymentExecutionResult.PaymentExtraDetails extraDetails;
    private PaymentFailure paymentFailure;


    public static PaymentStatusUpdateCommand from(PaymentExecutionResult result) {
        return PaymentStatusUpdateCommand.builder()
                .paymentKey(result.getPaymentKey())
                .orderId(result.getOrderId())
                .status(result.paymentStatus())
                .extraDetails(result.getExtraDetails())
                .paymentFailure(result.getPaymentFailure())
                .build();
    }

    @Builder
    public PaymentStatusUpdateCommand(String paymentKey, String orderId, PaymentStatus status, PaymentExecutionResult.PaymentExtraDetails extraDetails, PaymentFailure paymentFailure) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.status = status;
        this.extraDetails = extraDetails;
        this.paymentFailure = paymentFailure;
    }
}
