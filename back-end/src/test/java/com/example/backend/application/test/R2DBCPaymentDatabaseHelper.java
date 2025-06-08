package com.example.backend.application.test;

import com.example.backend.domain.PaymentEvent;
import com.example.backend.domain.PaymentOrder;
import com.example.backend.domain.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Objects;

public class R2DBCPaymentDatabaseHelper implements PaymentDatabaseHelper {

    private final DatabaseClient databaseClient;
    private final TransactionalOperator transactionalOperator;

    public R2DBCPaymentDatabaseHelper(
            @Autowired DatabaseClient databaseClient,
            @Autowired TransactionalOperator transactionalOperator
    ) {
        this.databaseClient = databaseClient;
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public PaymentEvent getPayment(String orderId) {
        return Objects.requireNonNull(Mono.from(databaseClient.sql(SELECT_PAYMENT_ORDER)
                .bind("orderId", orderId)
                .fetch()
                .all()
                .groupBy(resultMap -> resultMap.get("payment_event_id"))
                .flatMap(groupedFlux ->
                        groupedFlux.collectList().map(results -> {
                            boolean isPaymentDone = (Boolean) results.get(0).get("is_payment_done");

                            return PaymentEvent.builder()
                                    .id((Long) groupedFlux.key())
                                    .orderId((String) results.get(0).get("order_id"))
                                    .orderName((String) results.get(0).get("order_name"))
                                    .buyerId((Long) results.get(0).get("buyer_id"))
                                    .isPaymentDone(isPaymentDone)
                                    .paymentOrders(results.stream()
                                            .map(resultMap -> {
                                                boolean isLedgerUpdated = (Boolean) results.get(0).get("ledger_updated");
                                                boolean isWalletUpdated = (Boolean) results.get(0).get("wallet_updated");

                                                return PaymentOrder.builder()
                                                        .id((Long) resultMap.get("id"))
                                                        .orderId((String) resultMap.get("order_id"))
                                                        .productId((Long) resultMap.get("product_id"))
                                                        .sellerId((Long) resultMap.get("seller_id"))
                                                        .amount((BigDecimal) resultMap.get("amount"))
                                                        .paymentStatus(PaymentStatus.valueOf((String) resultMap.get("payment_order_status")))
                                                        .isLedgerUpdated(isLedgerUpdated)
                                                        .isWalletUpdated(isWalletUpdated)
                                                        .build();
                                            })
                                            .toList());
                        })
                )).block()).build();
    }

    @Override
    public void clear() {
        deletePaymentOrders()
                .then(deletePaymentEvents())
                .as(transactionalOperator::transactional)
                .block();
    }

    private Mono<Long> deletePaymentOrders() {
        return databaseClient.sql(DELETE_PAYMENT_ORDER).fetch().rowsUpdated();
    }
    private Mono<Long> deletePaymentEvents() {
        return databaseClient.sql(DELETE_PAYMENT_EVENT).fetch().rowsUpdated();
    }

    private static final String SELECT_PAYMENT_ORDER = """
        SELECT * FROM payment_event pe
        INNER JOIN payment_order po ON pe.id = po.payment_event_id
        WHERE pe.order_id = :orderId
    """.trim();

    private static final String DELETE_PAYMENT_EVENT = """
        DELETE FROM payment_event
    """.trim();

    private static final String DELETE_PAYMENT_ORDER = """
        DELETE FROM payment_order
    """.trim();
}
