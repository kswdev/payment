package com.example.backend.adaptor.in.web.api;

import com.example.backend.adaptor.in.web.dto.request.TossPaymentConfirmRequest;
import com.example.backend.adaptor.in.web.dto.response.ApiResponse;
import com.example.backend.adaptor.out.web.toss.executor.TossPaymentExecutor;
import com.example.backend.common.WebAdaptor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@WebAdaptor
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/toss")
public class TossPaymentController {

    private final TossPaymentExecutor tossPaymentExecutor;

    @PostMapping("/confirm")
    public Mono<ResponseEntity<ApiResponse<String>>> confirm(@RequestBody TossPaymentConfirmRequest request) {
        System.out.println("TossPaymentController confirm");
        return tossPaymentExecutor.execute(request.paymentKey(),
                                           request.orderId(),
                                           request.amount()
        ).map(response -> ResponseEntity.ok().body(
                ApiResponse.with(HttpStatus.OK.value(), "OK", response)));
    }
}

