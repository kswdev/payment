package com.example.backend.adapter.out.persistence.repository;

import com.example.backend.adapter.out.persistence.util.PartitionKeyUtil;
import com.example.backend.application.command.PaymentStatusUpdateCommand;
import com.example.backend.domain.PaymentEventMessage;
import com.example.backend.domain.PaymentEventMessage.Type;
import com.example.backend.domain.PaymentStatus;
import com.example.backend.adapter.out.stream.util.Mapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

import static com.example.backend.domain.PaymentEventMessage.Type.*;
import static com.example.backend.util.CustomDateTimeFormatter.*;

@Repository
@RequiredArgsConstructor
public class R2DBCPaymentOutboxRepository implements PaymentOutboxRepository {

    private final DatabaseClient databaseClient;

    @Override
    public Mono<Boolean> markMessageAsSent(String idempotencyKey, Type paymentEventMessageType) {
        return databaseClient.sql(UPDATE_OUTBOX_MESSAGE_SENT)
                .bind("idempotencyKey", idempotencyKey)
                .bind("type", paymentEventMessageType.name())
                .fetch()
                .rowsUpdated()
                .thenReturn(true);
    }

    @Override
    public Mono<Boolean> markMessageAsFailure(String idempotencyKey, Type paymentEventMessageType) {
        return databaseClient.sql(UPDATE_OUTBOX_MESSAGE_FAILURE)
                .bind("idempotencyKey", idempotencyKey)
                .bind("type", paymentEventMessageType.name())
                .fetch()
                .rowsUpdated()
                .thenReturn(true);
    }

    @Override
    public Flux<PaymentEventMessage> getPendingPaymentOutboxes() {
        return databaseClient.sql(SELECT_PENDING_PAYMENT_OUTBOX)
                .bind("createdAt", LocalDateTime.now().format(MYSQL_DATE_TIME_FORMATTER))
                .fetch()
                .all()
                .map(resultMap -> new PaymentEventMessage(
                        PAYMENT_CONFIRMATION_SUCCESS,
                        Mapper.readAsMap((String) resultMap.get("payload")),
                        Mapper.readAsMap((String) resultMap.get("metadata"))
                ));
    }

    @Override
    public Mono<PaymentEventMessage> insertOutbox(PaymentStatusUpdateCommand command) {
        assert command.getStatus() == PaymentStatus.SUCCESS;

        PaymentEventMessage eventMessage = createPaymentEventMessage(command);

        return databaseClient.sql(INSERT_OUTBOX)
                .bind("idempotencyKey", eventMessage.getPayload().get("orderId"))
                .bind("partitionKey", eventMessage.getMetadata().get("partitionKey"))
                .bind("type", eventMessage.getType().name())
                .bind("payload", MapToString(eventMessage.getPayload()))
                .bind("metadata", MapToString(eventMessage.getMetadata()))
                .fetch()
                .rowsUpdated()
                .thenReturn(eventMessage);
    }

    private static String MapToString(Map<String, ?> map) {
        try {
            return Mapper.om.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static PaymentEventMessage createPaymentEventMessage(PaymentStatusUpdateCommand command) {
        return new PaymentEventMessage(
                PAYMENT_CONFIRMATION_SUCCESS,
                Map.of("orderId", command.getOrderId()),
                Map.of("partitionKey", PartitionKeyUtil.createPartitionKey(command.getOrderId().hashCode()))
        );
    }

    private static final String SELECT_PENDING_PAYMENT_OUTBOX = """
        SELECT *
        FROM outboxes
        WHERE status = 'INIT' or status = 'FAILURE'
        AND created_at < :createdAt - INTERVAL 1 MINUTE
        AND type = 'PAYMENT_CONFIRMATION_SUCCESS'
    """.trim();

    private static final String UPDATE_OUTBOX_MESSAGE_SENT = """
        UPDATE outboxes
        SET status = 'SUCCESS'
        WHERE idempotency_key = :idempotencyKey
        and type = :type
    """.trim();

    private static final String UPDATE_OUTBOX_MESSAGE_FAILURE = """
        UPDATE outboxes
        SET status = 'FAILURE'
        WHERE idempotency_key = :idempotencyKey
        and type = :type
    """.trim();

    private static final String INSERT_OUTBOX = """
        INSERT INTO outboxes (idempotency_key, partition_key, type, payload, metadata)
        VALUES (:idempotencyKey, :partitionKey, :type, :payload, :metadata)
    """.trim();
}
