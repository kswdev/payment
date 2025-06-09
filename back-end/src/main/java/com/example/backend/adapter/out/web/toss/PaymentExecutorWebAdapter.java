package com.example.backend.adapter.out.web.toss;

import com.example.backend.adapter.out.web.toss.executor.PaymentExecutor;
import com.example.backend.application.command.PaymentConfirmCommand;
import com.example.backend.application.port.out.PaymentExecutorPort;
import com.example.backend.common.WebAdapter;
import com.example.backend.domain.PaymentExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@WebAdapter
@Component
@RequiredArgsConstructor
public class PaymentExecutorWebAdapter implements PaymentExecutorPort {

    private final PaymentExecutor paymentExecutor;

    @Override
    public Mono<PaymentExecutionResult> execute(PaymentConfirmCommand command) {
        return paymentExecutor.execute(command);
    }
}
