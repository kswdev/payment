package com.example.backend.adapter.in.web.dto.request;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CheckOutRequest(
    Long cartId,
    List<Long> productIds,
    Long buyerId,
    String seed
) {

    public static CheckOutRequest defaultRequest() {
        return new CheckOutRequest(
                1L,
                List.of(1L, 2L, 3L),
                1L,
                UUID.randomUUID().toString());
    }
}
