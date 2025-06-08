package com.example.backend.adapter.in.web.dto.request;

import java.time.LocalDateTime;
import java.util.List;

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
                LocalDateTime.now().toString());
    }
}
