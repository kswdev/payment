package com.example.backend.adapter.out.persistence.repository;

import com.example.backend.domain.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class R2DBCPaymentRepository implements PaymentRepository {

    private final DatabaseClient databaseClient;
    private final TransactionalOperator transactionalOperator;

    private static final String INSERT_PAYMENT_EVENT = """
                INSERT INTO payment_event (buyer_id, order_name, order_id)
                VALUES (:buyerId, :orderName, :orderId)
            """.trim();

    private static final String LAST_INSERT_ID = "SELECT LAST_INSERT_ID()".trim();

    private static final String INSERT_PAYMENT_ORDER_QUERY = """
                INSERT INTO payment_order (payment_event_id, seller_id, order_id, product_id, amount, order_status)
                VALUES
    """.trim();

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
}
