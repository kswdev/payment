package com.example.backend.adapter.out.persistence.repository;

import com.example.backend.domain.PaymentEvent;
import com.example.backend.domain.PaymentStatus;
import com.example.backend.domain.PendingPaymentEvent;
import com.example.backend.domain.PendingPaymentOrder;
import com.example.backend.util.CustomDateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.dialect.MySqlDialect;
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
                                                                .paymentStatus(PaymentStatus.get((String) resultMap.get("payment_status")))
                                                                .amount(((BigDecimal) resultMap.get("amount")).longValue())
                                                                .failedCount((Integer) resultMap.get("failed_count"))
                                                                .threshold((Integer) resultMap.get("threshold"))
                                                                .build())
                                                .toList()
                                )
                                .build()
                        )
                );
    }

    @Override
    public Mono<Void> save(PaymentEvent paymentEvent) {
        return insertPaymentEvent(paymentEvent)
                .flatMap(notUsed -> selectPaymentEventId())
                .flatMap(paymentEventId -> insertPaymentOrders(paymentEvent, paymentEventId))
                .as(transactionalOperator::transactional)
                .then();
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
    private static final String SELECT_PENDING_PAYMENT_EVENTS = """
                SELECT pe.id as payment_event_id,
                       pe.order_id,
                       pe.payment_key,
                       po.payment_order_id,
                       po.payment_status,
                       po.amount,
                       po.failed_count,
                       po.threshold,
                  FROM payment_event pe
                  INNER JOIN payment_order po ON pe.id = po.payment_event_id
                  WHERE po.payment_status = 'UNKNOWN' OR (po.payment_status = 'EXECUTING' AND po.updated_at <= :updatedAt - INTERVAL 3 MINUTE )
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
}
