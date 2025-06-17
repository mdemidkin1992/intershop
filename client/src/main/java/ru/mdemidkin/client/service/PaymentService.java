package ru.mdemidkin.client.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.mdemidkin.intershop.client.api.HealthApi;
import ru.mdemidkin.intershop.client.api.PaymentsApi;
import ru.mdemidkin.intershop.client.domain.BalanceResponse;
import ru.mdemidkin.intershop.client.domain.PaymentRequest;
import ru.mdemidkin.intershop.client.domain.PaymentResponse;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final HealthApi healthApi;
    private final PaymentsApi paymentsApi;

    public Mono<Boolean> processOrderPayment(Double totalPrice, String username) {
        return checkHealth()
                .flatMap(healthStatus -> checkBalance(healthStatus, totalPrice, username))
                .flatMap(balanceStatus -> processPayment(balanceStatus, totalPrice, username));
    }

    private Mono<Boolean> checkHealth() {
        return healthApi.healthCheck()
                .map(result -> true);
    }

    private Mono<Boolean> checkBalance(Boolean healthStatus, Double totalPrice, String userId) {
        if (!healthStatus) {
            return Mono.just(false);
        }
        return paymentsApi.getBalance(userId)
                .map(balance -> isSufficientBalance(balance, totalPrice));
    }

    private Mono<Boolean> processPayment(Boolean balanceStatus, Double totalPrice, String userId) {
        if (!balanceStatus) {
            return Mono.just(false);
        }

        final PaymentRequest paymentRequest = new PaymentRequest()
                .userId(userId)
                .amount(BigDecimal.valueOf(totalPrice));

        return paymentsApi.processPayment(paymentRequest)
                .map(this::isSuccessfulPayment);
    }

    private Boolean isSufficientBalance(BalanceResponse balance, Double totalPrice) {
        return balance != null && balance.getBalance().compareTo(new BigDecimal(totalPrice)) >= 0;
    }

    private Boolean isSuccessfulPayment(PaymentResponse payment) {
        return payment != null && PaymentResponse.StatusEnum.SUCCESS.equals(payment.getStatus());
    }
}
