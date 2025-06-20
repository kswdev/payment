package com.example.backend.adapter.out.web.product;

import com.example.backend.adapter.out.web.product.client.ProductClient;
import com.example.backend.application.port.out.LoadProductPort;
import com.example.backend.common.WebAdapter;
import com.example.backend.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@WebAdapter
@RequiredArgsConstructor
public class ProductWebAdapter implements LoadProductPort {

    private final ProductClient productClient;

    @Override
    public Flux<Product> getProducts(Long cartId, List<Long> productIds) {
        return productClient.getProducts(cartId, productIds);
    }
}
