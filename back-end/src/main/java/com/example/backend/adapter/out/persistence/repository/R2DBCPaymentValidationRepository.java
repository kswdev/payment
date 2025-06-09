package com.example.backend.adapter.out.persistence.repository;

import com.example.backend.adapter.out.persistence.exception.PaymentValidationException;
import com.example.backend.common.PersistenceAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Component
@PersistenceAdapter
@RequiredArgsConstructor
public class R2DBCPaymentValidationRepository implements PaymentValidationRepository {

    private final DatabaseClient databaseClient;
    private final TransactionalOperator transactionalOperator;


    @Override
    public Mono<Boolean> isValid(String orderId, Long amount) {
        return databaseClient.sql(SELECT_PAYMENT_TOTAL_AMOUNT)
                .bind("orderId", orderId)
                .fetch()
                .first()
                .handle((row, sink) -> {
                    if (((BigDecimal) row.get("total_amount")).longValue() == amount)
                        sink.next(true);
                    else
                        sink.error(new PaymentValidationException("결제에서 금액이 맞지 않습니다. 결제 ID: " + orderId));
                });
    }

    private static final String SELECT_PAYMENT_TOTAL_AMOUNT = """
        SELECT SUM(amount) AS total_amount
        FROM payment_order
        WHERE order_id = :orderId
    """.trim();
}
