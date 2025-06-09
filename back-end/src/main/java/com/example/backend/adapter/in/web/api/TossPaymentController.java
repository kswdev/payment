package com.example.backend.adapter.in.web.api;

import com.example.backend.adapter.in.web.dto.request.TossPaymentConfirmRequest;
import com.example.backend.adapter.in.web.dto.response.ApiResponse;
import com.example.backend.application.command.PaymentConfirmCommand;
import com.example.backend.application.port.in.PaymentConfirmUseCase;
import com.example.backend.common.WebAdapter;
import com.example.backend.domain.PaymentConfirmationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@WebAdapter
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/toss")
public class TossPaymentController {

    private final PaymentConfirmUseCase paymentConfirmUseCase;

    @PostMapping("/confirm")
    public Mono<ResponseEntity<ApiResponse<PaymentConfirmationResult>>> confirm(@RequestBody TossPaymentConfirmRequest request) {
        PaymentConfirmCommand command = new PaymentConfirmCommand(
                request.paymentKey(),
                request.orderId(),
                request.amount()
        );

        return paymentConfirmUseCase.confirm(command)
                .map(paymentConfirmResult ->
                        ResponseEntity.ok().body(ApiResponse.with(HttpStatus.OK.value(), "", paymentConfirmResult)));
    }
}

