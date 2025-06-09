package com.example.backend.domain;

import com.example.backend.adapter.out.web.toss.exception.TossPaymentError;
import com.example.backend.adapter.out.web.toss.response.TossPaymentConfirmationResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PaymentExecutionResult {
    private String paymentKey;
    private String orderId;
    private PaymentExtraDetails extraDetails;
    private PaymentFailure paymentFailure;
    private Boolean isSuccess = null;
    private Boolean isFailure = null;
    private Boolean isUnknown = null;

    @Builder
    public PaymentExecutionResult(String paymentKey, String orderId, PaymentExtraDetails extraDetails, PaymentFailure paymentFailure, Boolean isSuccess, Boolean isFailure, Boolean isUnknown) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.extraDetails = extraDetails;
        this.paymentFailure = paymentFailure;
        this.isSuccess = isSuccess;
        this.isFailure = isFailure;
        this.isUnknown = isUnknown;
    }

    public PaymentStatus paymentStatus() {
        if (isSuccess) {
            return PaymentStatus.SUCCESS;
        } else if (isFailure) {
            return PaymentStatus.FAILURE;
        } else {
            return PaymentStatus.UNKNOWN;
        }
    }


    public static PaymentExecutionResult fromTossPaymentConfirmResponse(TossPaymentConfirmationResponse response) {
        TossPaymentError error = TossPaymentError.get(response.getStatus());
        PaymentExtraDetails details = new PaymentExtraDetails(
                PaymentType.valueOf(response.getType()),
                PaymentMethod.valueOf(response.getMethod()),
                response.getApprovedAt(),
                response.getOrderName(),
                PSPConfirmationStatus.valueOf(response.getStatus()),
                response.getTotalAmount(),
                response.toString()
        );

        return PaymentExecutionResult.builder()
                .paymentKey(response.getPaymentKey())
                .orderId(response.getOrderId())
                .extraDetails(details)
                .isFailure(error.isFailure())
                .isSuccess(error.isSuccess())
                .isUnknown(error.isUnknown())
                .build();
    }

    @Getter
    public static class PaymentExtraDetails {
        private PaymentType paymentType;
        private PaymentMethod paymentMethod;
        private LocalDateTime approvedAt;
        private String orderName;
        private PSPConfirmationStatus pspConfirmationStatus;
        private Long totalAmount;
        private String rawData;

        public PaymentExtraDetails(PaymentType paymentType, PaymentMethod paymentMethod, LocalDateTime approvedAt, String orderName, PSPConfirmationStatus pspConfirmationStatus, Long totalAmount, String rawData) {
            this.paymentType = paymentType;
            this.paymentMethod = paymentMethod;
            this.approvedAt = approvedAt;
            this.orderName = orderName;
            this.pspConfirmationStatus = pspConfirmationStatus;
            this.totalAmount = totalAmount;
            this.rawData = rawData;
        }
    }

    @Getter
    public static class PaymentFailure {
        private String errorCode;
        private String message;

        public PaymentFailure(String errorCode, String message) {
            this.errorCode = errorCode;
            this.message = message;
        }
    }
}
