package ru.mdemidkin.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.mdemidkin.intershop.server.api.PaymentsApi;
import ru.mdemidkin.intershop.server.domain.BalanceResponse;
import ru.mdemidkin.intershop.server.domain.PaymentRequest;
import ru.mdemidkin.intershop.server.domain.PaymentResponse;
import ru.mdemidkin.server.service.PaymentService;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class PaymentsApiController implements PaymentsApi {

    private final PaymentService paymentService;

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getBalance(String userId, ServerWebExchange exchange) {
        return paymentService.getBalance(userId)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> processPayment(Mono<PaymentRequest> paymentRequestMono, ServerWebExchange exchange) {
        return paymentRequestMono
                .flatMap(paymentService::processPayment)
                .map(this::mapResponse)
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }

    private ResponseEntity<PaymentResponse> mapResponse(PaymentResponse paymentResponse) {
        if (PaymentResponse.StatusEnum.SUCCESS.equals(paymentResponse.getStatus())) {
            return ResponseEntity.ok(paymentResponse);
        } else {
            return ResponseEntity.badRequest().body(paymentResponse);
        }
    }
}
