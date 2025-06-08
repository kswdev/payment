package com.example.backend.application.port.out;

import com.example.backend.domain.Product;
import reactor.core.publisher.Flux;

import java.util.List;

public interface LoadProductPort {

    Flux<Product> getProducts(Long cartId, List<Long> productIds);
}
