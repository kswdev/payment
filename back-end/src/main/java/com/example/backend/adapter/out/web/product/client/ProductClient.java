package com.example.backend.adapter.out.web.product.client;

import com.example.backend.domain.Product;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ProductClient {

    Flux<Product> getProducts(Long cartId, List<Long> productIds);
}
