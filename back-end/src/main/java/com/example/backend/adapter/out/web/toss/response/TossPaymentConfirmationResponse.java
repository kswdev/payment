package com.example.backend.adapter.out.web.toss.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class TossPaymentConfirmationResponse {
    private String version;
    private String paymentKey;
    private String type;
    private String orderId;
    private String orderName;
    private String mId;
    private String currency;
    private String method;
    private Long totalAmount;
    private Long balanceAmount;
    private String status;
    private String requestedAt;
    private String approvedAt;
    private Boolean useEscrow;
    private String lastTransactionKey;
    private Long suppliedAmount;
    private Long vat;
    private Boolean cultureExpense;
    private Long taxFreeAmount;
    private Integer taxExemptionAmount;
    private List<Cancel> cancels;
    private Boolean isPartialCancelable;
    private Card card;
    private VirtualAccount virtualAccount;
    private MobilePhone mobilePhone;
    private GiftCertificate giftCertificate;
    private Transfer transfer;
    private Map<String, String> metadata;
    private Receipt receipt;
    private Checkout checkout;
    private EasyPay easyPay;
    private String country;
    private TossFailureResponse tossFailureResponse;
    private CashReceipt cashReceipt;
    private List<CashReceiptDetail> cashReceipts;
    private Discount discount;

    @Getter
    @NoArgsConstructor
    public static class Cancel {
        private Long cancelAmount;
        private String cancelReason;
        private Long taxFreeAmount;
        private Integer taxExemptionAmount;
        private Long refundableAmount;
        private Long transferDiscountAmount;
        private Long easyPayDiscountAmount;
        private String canceledAt;
        private String transactionKey;
        private String receiptKey;
        private String cancelStatus;
        private String cancelRequestId;
    }

    @Getter
    @NoArgsConstructor
    public static class Card {
        private Long amount;
        private String issuerCode;
        private String acquirerCode;
        private String number;
        private Integer installmentPlanMonths;
        private String approveNo;
        private Boolean useCardPoint;
        private String cardType;
        private String ownerType;
        private String acquireStatus;
        private Boolean isInterestFree;
        private String interestPayer;
    }

    @Getter
    @NoArgsConstructor
    public static class VirtualAccount {
        private String accountType;
        private String accountNumber;
        private String bankCode;
        private String customerName;
        private String dueDate;
        private String refundStatus;
        private Boolean expired;
        private String settlementStatus;
        private RefundReceiveAccount refundReceiveAccount;
        private String secret;
    }

    @Getter
    @NoArgsConstructor
    public static class RefundReceiveAccount {
        private String bankCode;
        private String accountNumber;
        private String holderName;
    }

    @Getter
    @NoArgsConstructor
    public static class MobilePhone {
        private String customerMobilePhone;
        private String settlementStatus;
        private String receiptUrl;
    }

    @Getter
    @NoArgsConstructor
    public static class GiftCertificate {
        private String approveNo;
        private String settlementStatus;
    }

    @Getter
    @NoArgsConstructor
    public static class Transfer {
        private String bankCode;
        private String settlementStatus;
    }

    @Getter
    @NoArgsConstructor
    public static class Receipt {
        private String url;
    }

    @Getter
    @NoArgsConstructor
    public static class Checkout {
        private String url;
    }

    @Getter
    @NoArgsConstructor
    public static class EasyPay {
        private String provider;
        private Long amount;
        private Long discountAmount;
    }

    @Getter
    @NoArgsConstructor
    public static class TossFailureResponse {
        private String code;
        private String message;
    }

    @Getter
    @NoArgsConstructor
    public static class CashReceipt {
        private String type;
        private String receiptKey;
        private String issueNumber;
        private String receiptUrl;
        private Long amount;
        private Long taxFreeAmount;
    }

    @Getter
    @NoArgsConstructor
    public static class CashReceiptDetail {
        private String receiptKey;
        private String orderId;
        private String orderName;
        private String type;
        private String issueNumber;
        private String receiptUrl;
        private String businessNumber;
        private String transactionType;
        private Integer amount;
        private Integer taxFreeAmount;
        private String issueStatus;
        private TossFailureResponse tossFailureResponse;
        private String customerIdentityNumber;
        private String requestedAt;
    }

    @Getter
    @NoArgsConstructor
    public static class Discount {
        private Integer amount;
    }
}
