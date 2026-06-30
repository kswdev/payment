package com.example.backend.adapter.out.persistence.repository;

import com.example.backend.domain.*;
import com.example.backend.util.CustomDateTimeFormatter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class R2DBCPaymentRepository implements PaymentRepository {

    private final DatabaseClient databaseClient;
    private final TransactionalOperator transactionalOperator;
    private final ObjectMapper objectMapper;

    @Override
    public Flux<PendingPaymentEvent> getPendingPayments() {
        return databaseClient.sql(SELECT_PENDING_PAYMENT_EVENTS)
                .bind("updatedAt", LocalDateTime.now().format(CustomDateTimeFormatter.MYSQL_DATE_TIME_FORMATTER))
                .fetch()
                .all()
                .groupBy(resultMap -> (Long) resultMap.get("payment_event_id"))
                .flatMap(groupedFlux ->
                        groupedFlux.collectList().map(results -> PendingPaymentEvent.builder()
                                .paymentEventId(groupedFlux.key())
                                .paymentKey((String) results.getFirst().get("payment_key"))
                                .orderId((String) results.getFirst().get("order_id"))
                                .pendingPaymentOrders(
                                        results.stream()
                                                .map(resultMap ->
                                                        PendingPaymentOrder.builder()
                                                                .paymentOrderId((Long) resultMap.get("payment_order_id"))
                                                                .paymentStatus(PaymentStatus.get((String) resultMap.get("order_status")))
                                                                .amount(((BigDecimal) resultMap.get("amount")).longValue())
                                                                .failedCount((Byte) resultMap.get("failed_count"))
                                                                .threshold((Byte) resultMap.get("threshold"))
                                                                .build())
                                                .toList()
                                )
                                .build()
                        )
                );
    }

    @Override
    public Mono<Void> complete(PaymentEvent paymentEvent) {
        if (paymentEvent.isPaymentDone()) {
            return handlePaymentCompletion(paymentEvent);
        } else if (paymentEvent.isLedgerUpdateDone()) {
            return handleLedgerUpdate(paymentEvent);
        } else if (paymentEvent.isWalletUpdateDone()) {
            return handleWalletUpdate(paymentEvent);
        } else {
            throw new IllegalStateException("Incorrect state for PaymentEvent id: " + paymentEvent.getId());
        }
    }

    @Override
    public Mono<Void> save(PaymentEvent paymentEvent) {
        return insertPaymentEvent(paymentEvent)
                .flatMap(notUsed -> selectPaymentEventId())
                .flatMap(paymentEventId -> insertPaymentOrders(paymentEvent, paymentEventId))
                .as(transactionalOperator::transactional)
                .then();
    }

    @Override
    public Mono<PaymentEvent> getPayment(String orderId) {
        return databaseClient.sql(SELECT_PAYMENT_EVENT)
                .bind("order_id", orderId)
                .fetch()
                .all()
                .collectList()
                .map(results -> PaymentEvent.builder()
                                .id((Long) results.getFirst().get("payment_event_id"))
                                .orderId((String) results.getFirst().get("order_id"))
                                .buyerId((Long) results.getFirst().get("buyer_id"))
                                .orderName((String) results.getFirst().get("order_name"))
                                .isPaymentDone((int) results.getFirst().get("is_payment_done") == 1)
                                .paymentOrders(
                                        results.stream()
                                                .map(result -> PaymentOrder.builder()
                                                        .id((Long) result.get("payment_order_id"))
                                                        .paymentEventId((Long) results.getFirst().get("payment_event_id"))
                                                        .sellerId((Long) result.get("seller_id"))
                                                        .orderId((String) result.get("order_id"))
                                                        .amount((Long) result.get("amount"))
                                                        .paymentStatus(PaymentStatus.get((String) result.get("payment_order_status")))
                                                        .isLedgerUpdated((int) result.get("ledger_updated") == 1)
                                                        .isLedgerUpdated((int) result.get("wallet_updated") == 1)
                                                        .build()
                                                )
                                                .toList()
                                )
                                .build()
                );
    }

    private Mono<Long> insertPaymentOrders(PaymentEvent paymentEvent, Long paymentEventId) {
        String collect = paymentEvent.getPaymentOrders().stream()
                .map(paymentOrder -> String.format("(%s, %s, '%s', %s, %s, '%s')",
                        paymentEventId, paymentOrder.getSellerId(), paymentOrder.getOrderId(), paymentOrder.getProductId(), paymentOrder.getAmount(), paymentOrder.getPaymentStatus()))
                .collect(Collectors.joining(", "));

        return databaseClient.sql(INSERT_PAYMENT_ORDER_QUERY + " " + collect)
                .fetch()
                .rowsUpdated();
    }

    private Mono<Long> selectPaymentEventId() {
        return databaseClient.sql(LAST_INSERT_ID)
                .fetch()
                .first()
                .map(resultMap -> ((BigInteger) resultMap.get("LAST_INSERT_ID()")).longValue());

    }

    private Mono<Long> insertPaymentEvent(PaymentEvent paymentEvent) {
        return databaseClient
                .sql(INSERT_PAYMENT_EVENT)
                .bind("buyerId", paymentEvent.getBuyerId())
                .bind("orderName", paymentEvent.getOrderName())
                .bind("orderId", paymentEvent.getOrderId())
                .fetch()
                .rowsUpdated();
    }

    private Mono<Void> handlePaymentCompletion(PaymentEvent paymentEvent) {
        return Mono.when(
                handleWalletUpdate(paymentEvent),
                handleLedgerUpdate(paymentEvent)
        ).then(Mono.defer(() -> completePaymentEvent(paymentEvent)));
    }

    private Mono<Void> completePaymentEvent(PaymentEvent paymentEvent) {
        return databaseClient.sql(UPDATE_PAYMENT_EVENT_DONE)
                .bind("orderId", paymentEvent.getOrderId())
                .fetch()
                .rowsUpdated()
                .then();
    }

    private Mono<Void> handleWalletUpdate(PaymentEvent paymentEvent) {
        return databaseClient
                .sql(UPDATE_PAYMENT_ORDER_WALLET_DONE)
                .bind("orderId", paymentEvent.getOrderId())
                .fetch()
                .rowsUpdated()
                .then();
    }

    private Mono<Void> handleLedgerUpdate(PaymentEvent paymentEvent) {
        return databaseClient
                .sql(UPDATE_PAYMENT_ORDER_LEDGER_DONE)
                .bind("orderId", paymentEvent.getOrderId())
                .fetch()
                .rowsUpdated()
                .then();
    }

    private static final String SELECT_PENDING_PAYMENT_EVENTS = """
                SELECT pe.id as payment_event_id,
                       pe.order_id,
                       pe.payment_key,
                       po.order_id,
                       po.order_status,
                       po.amount,
                       po.failed_count,
                       po.threshold
                  FROM payment_event pe
                  INNER JOIN payment_order po ON pe.id = po.payment_event_id
                  WHERE po.order_status = 'UNKNOWN' OR (po.order_status = 'EXECUTING' AND po.updated_at <= :updatedAt - INTERVAL 3 MINUTE )
                    AND po.failed_count < po.threshold
                  LIMIT 10
            """.trim();

    private static final String INSERT_PAYMENT_EVENT = """
                INSERT INTO payment_event (buyer_id, order_name, order_id)
                VALUES (:buyerId, :orderName, :orderId)
            """.trim();

    private static final String LAST_INSERT_ID = "SELECT LAST_INSERT_ID()".trim();

    private static final String INSERT_PAYMENT_ORDER_QUERY = """
                INSERT INTO payment_order (payment_event_id, seller_id, order_id, product_id, amount, order_status)
                VALUES
    """.trim();

    private static final String SELECT_PAYMENT_EVENT = """
                SELECT pe.id as payment_event_id,
                       po.id as payment_order_id,
                       pe.order_id,
                       pe.order_name,
                       pe.buyer_id,
                       pe.payment_key,
                       pe.type as payment_type,
                       pe.method as payment_method,
                       pe.approved_at,
                       pe.is_payment_done,
                       po.seller_id,
                       po.product_id,
                       po.order_status as payment_order_status,
                       po.amount,
                       po.ledger_updated,
                       po.wallet_updated,
                  FROM payment_event pe
            INNER JOIN payment_order po
                    ON pe.order_id = po.order_id
                 WHERE pe.order_id = :order_id
    """.trim();

    private static final String UPDATE_PAYMENT_ORDER_LEDGER_DONE = """
        UPDATE payment_order
           SET ledger_updated = true
         WHERE order_id = :orderId
    """.trim();

    private static final String UPDATE_PAYMENT_ORDER_WALLET_DONE = """
        UPDATE payment_order
           SET wallet_updated = true
         WHERE order_id = :orderId
    """.trim();

    private static final String UPDATE_PAYMENT_EVENT_DONE = """
        UPDATE payment_event
           SET is_payment_done = true
         WHERE order_id = :orderId
    """.trim();
}
