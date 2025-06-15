package com.example.backend.adapter.out.persistence.repository;

import com.example.backend.adapter.out.persistence.exception.PaymentAlreadyProcessedException;
import com.example.backend.application.command.PaymentStatusUpdateCommand;
import com.example.backend.domain.PaymentEventMessagePublisher;
import com.example.backend.domain.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class R2DBCPaymentStatusUpdateRepository implements PaymentStatusUpdateRepository {

    private final DatabaseClient databaseClient;
    private final TransactionalOperator transactionalOperator;
    private final R2DBCPaymentOutboxRepository paymentOutboxRepository;
    private final PaymentEventMessagePublisher paymentEventPublisher;


    @Override
    public Mono<Boolean> updatePaymentStatusToExecuting(String orderId, String paymentKey) {
        return checkPreviousPaymentOrderStatus(orderId)
                .flatMap(pairs -> insertPaymentHistory(pairs, PaymentStatus.EXECUTING, "PAYMENT_CONFIRMATION_START"))
                .then(updatePaymentOrderStatus(orderId, PaymentStatus.EXECUTING))
                .then(updatePaymentKey(orderId, paymentKey))
                .as(transactionalOperator::transactional)
                .thenReturn(true);
    }

    @Override
    public Mono<Boolean> updatePaymentStatus(PaymentStatusUpdateCommand command) {
        return switch (command.getStatus()) {
            case SUCCESS -> updatePaymentStatusToSuccess(command);
            case FAILURE -> updatePaymentStatusToFailure(command);
            case UNKNOWN -> updatePaymentStatusToUnknown(command);
            default -> Mono.error(() -> new IllegalStateException("올바르지 않은 결제 상태입니다."));
        };
    }

    private Mono<List<Pair<Long, String>>> checkPreviousPaymentOrderStatus(String orderId) {
        return selectPaymentOrderStatus(orderId)
                .<Pair<Long, String>>handle((paymentOrder, sink) -> {
                    switch (PaymentStatus.valueOf(paymentOrder.getSecond())) {
                        case NOT_STARTED,
                             EXECUTING,
                             UNKNOWN -> sink.next(paymentOrder);
                        case SUCCESS -> sink.error(new PaymentAlreadyProcessedException("이미 처리 성공된 결제입니다.", PaymentStatus.SUCCESS));
                        case FAILURE -> sink.error(new PaymentAlreadyProcessedException("이미 처리 실패한 결제입니다.", PaymentStatus.FAILURE));
                    }
                }).collectList();
    }
    private Mono<Boolean> updatePaymentStatusToUnknown(PaymentStatusUpdateCommand command) {
        return selectPaymentOrderStatus(command.getOrderId())
                .collectList()
                .flatMap(paymentOrderIdToStatus -> insertPaymentHistory(paymentOrderIdToStatus, command.getStatus(), command.getPaymentFailure().getMessage()))
                .then(updatePaymentOrderStatus(command.getOrderId(), command.getStatus()))
                .then(incrementPaymentFailureCount(command))
                .as(transactionalOperator::transactional)
                .thenReturn(true);
    }

    private Mono<Boolean> updatePaymentStatusToFailure(PaymentStatusUpdateCommand command) {
        return selectPaymentOrderStatus(command.getOrderId())
                .collectList()
                .flatMap(paymentOrderIdToStatus -> insertPaymentHistory(paymentOrderIdToStatus, command.getStatus(), command.getPaymentFailure().toString()))
                .then(updatePaymentOrderStatus(command.getOrderId(), command.getStatus()))
                .as(transactionalOperator::transactional)
                .thenReturn(true);
    }

    private Mono<Boolean> updatePaymentStatusToSuccess(PaymentStatusUpdateCommand command) {
        return selectPaymentOrderStatus(command.getOrderId())
                .collectList()
                .flatMap(paymentOrderIdToStatus -> insertPaymentHistory(paymentOrderIdToStatus, command.getStatus(), "PAYMENT_CONFIRMATION_DONE"))
                .then(updatePaymentOrderStatus(command.getOrderId(), command.getStatus()))
                .then(updatePaymentEventExtraDetails(command))
                .then(paymentOutboxRepository.insertOutbox(command))
                .flatMap(paymentEventPublisher::publishEvent)
                .as(transactionalOperator::transactional)
                .thenReturn(true);

    }

    private Mono<Long> incrementPaymentFailureCount(PaymentStatusUpdateCommand command) {
        return databaseClient.sql(INCREMENT_PAYMENT_FAILURE_COUNT)
                .bind("orderId", command.getOrderId())
                .fetch()
                .rowsUpdated();
    }

    private Mono<Long> updatePaymentEventExtraDetails(PaymentStatusUpdateCommand command) {
        return databaseClient.sql(UPDATE_PAYMENT_EVENT_EXTRA_DETAILS)
                .bind("orderId", command.getOrderId())
                .bind("orderName", command.getExtraDetails().getOrderName())
                .bind("method", command.getExtraDetails().getPaymentMethod())
                .bind("type", command.getExtraDetails().getPaymentType())
                .bind("approvedAt", command.getExtraDetails().getApprovedAt())
                .fetch()
                .rowsUpdated();
    }

    private Mono<Long> updatePaymentKey(String orderId, String paymentKey) {
        return databaseClient.sql(UPDATE_PAYMENT_KEY)
                .bind("orderId", orderId)
                .bind("paymentKey", paymentKey)
                .fetch()
                .rowsUpdated();
    }

    private Mono<Long> updatePaymentOrderStatus(String orderId, PaymentStatus status) {
        return databaseClient.sql(UPDATE_PAYMENT_ORDER_STATUS)
                .bind("orderId", orderId)
                .bind("orderStatus", status)
                .fetch()
                .rowsUpdated();
    }

    private Mono<Long> insertPaymentHistory(List<Pair<Long, String>> paymentOrderIdToStatus, PaymentStatus status, String reason) {
        if (paymentOrderIdToStatus.isEmpty()) return Mono.empty();

        String collect = paymentOrderIdToStatus.stream()
                .map(pair -> String.format("(%s, '%s', '%s', '%s')",
                        pair.getFirst(), pair.getSecond(), status.name(), reason))
                .collect(Collectors.joining(", "));

        return databaseClient.sql(INSERT_PAYMENT_ORDER_HISTORY + " " + collect)
                .fetch()
                .rowsUpdated();
    }

    private Flux<Pair<Long, String>> selectPaymentOrderStatus(String orderId) {
        return databaseClient.sql(SELECT_PAYMENT_ORDER_STATUS)
                .bind("orderId", orderId)
                .fetch()
                .all()
                .map(resultMap ->
                        Pair.of((Long) resultMap.get("id"),
                                (String) resultMap.get("order_status")));
    }

    private static final String INCREMENT_PAYMENT_FAILURE_COUNT = """
        UPDATE payment_order
        SET failed_count = failed_count + 1
        WHERE order_id = :orderId
    """.trim();

    private static final String UPDATE_PAYMENT_EVENT_EXTRA_DETAILS = """
        UPDATE payment_event
        SET order_name = :orderName, method = :method, approved_at = :approvedAt, type = :type, updated_at = CURRENT_TIMESTAMP()
        WHERE order_id = :orderId
    """.trim();

    private static final String UPDATE_PAYMENT_KEY = """
        UPDATE payment_event
        SET payment_key = :paymentKey
        WHERE order_id = :orderId
    """.trim();

    private static final String UPDATE_PAYMENT_ORDER_STATUS = """
        UPDATE payment_order
        SET order_status = :orderStatus, updated_at = CURRENT_TIMESTAMP()
        WHERE order_id = :orderId
    """.trim();

    private static final String INSERT_PAYMENT_ORDER_HISTORY = """
        INSERT INTO payment_order_histories (payment_order_id, previous_status, new_status, reason)
        VALUES
    """;

    private static final String SELECT_PAYMENT_ORDER_STATUS = """
        SELECT id, order_status FROM payment_order WHERE order_id = :orderId
    """.trim();
}
