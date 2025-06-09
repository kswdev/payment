package com.example.backend.adapter.in.web.api;

import com.example.backend.adapter.in.web.dto.request.CheckOutRequest;
import com.example.backend.application.command.CheckoutCommand;
import com.example.backend.application.port.in.CheckoutUseCase;
import com.example.backend.common.WebAdapter;
import com.example.backend.domain.CheckoutResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@WebAdapter
@RestController
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutUseCase checkoutUseCase;

    @GetMapping("/checkout")
    public Mono<CheckoutResult> checkout(
            //@RequestBody CheckOutRequest request
    ) {
        CheckOutRequest request = CheckOutRequest.defaultRequest();
        CheckoutCommand command = new CheckoutCommand(
                request.cartId(),
                request.productIds(),
                request.buyerId(),
                request.seed()
        );
        return checkoutUseCase.checkout(command);
    }
}
