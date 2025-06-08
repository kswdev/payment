package com.example.backend.adapter.out.web.product.client;

import com.example.backend.domain.Product;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.List;

@Component
public class MockProductClient implements ProductClient
{
    @Override
    public Flux<Product> getProducts(Long cartId, List<Long> productIds) {
        return Flux.fromIterable(
                productIds.stream()
                        .map(id -> new Product(
                                id,
                                BigDecimal.valueOf(id * 1000),
                                2,
                                "test-product-" + id,
                                1L
                        )).toList()
        );
    }
}
