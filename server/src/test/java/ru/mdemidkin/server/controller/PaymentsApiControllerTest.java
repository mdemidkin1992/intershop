package ru.mdemidkin.server.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.mdemidkin.intershop.server.domain.BalanceResponse;
import ru.mdemidkin.intershop.server.domain.PaymentRequest;
import ru.mdemidkin.intershop.server.domain.PaymentResponse;
import ru.mdemidkin.server.service.PaymentService;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(PaymentsApiController.class)
class PaymentsApiControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private PaymentService paymentService;

    @Test
    void getBalance_ShouldReturnBalance_WhenUserExists() {
        String userId = "user123";
        BalanceResponse balanceResponse = new BalanceResponse(userId, new BigDecimal("1500.00"));
        when(paymentService.getBalance(eq(userId))).thenReturn(Mono.just(balanceResponse));

        webTestClient.get()
                .uri("/payments/balance/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.userId").isEqualTo(userId)
                .jsonPath("$.balance").isEqualTo(1500.00);
    }

    @Test
    void getBalance_ShouldReturnInternalServerError_WhenServiceThrowsException() {
        String userId = "user123";
        when(paymentService.getBalance(eq(userId))).thenReturn(Mono.error(new RuntimeException("Service error")));

        webTestClient.get()
                .uri("/payments/balance/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void processPayment_ShouldReturnSuccess_WhenPaymentIsSuccessful() {
        PaymentRequest paymentRequest = new PaymentRequest()
                .userId("user123")
                .amount(new BigDecimal("100.00"));

        PaymentResponse paymentResponse = new PaymentResponse(
                "user123",
                new BigDecimal("100.00"),
                new BigDecimal("1400.00"),
                PaymentResponse.StatusEnum.SUCCESS,
                "Платеж успешно обработан"
        );

        when(paymentService.processPayment(any(PaymentRequest.class)))
                .thenReturn(Mono.just(paymentResponse));

        webTestClient.post()
                .uri("/payments/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(paymentRequest)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.userId").isEqualTo("user123")
                .jsonPath("$.amount").isEqualTo(100.00)
                .jsonPath("$.remainingBalance").isEqualTo(1400.00)
                .jsonPath("$.status").isEqualTo("SUCCESS")
                .jsonPath("$.message").isEqualTo("Платеж успешно обработан");
    }

    @Test
    void processPayment_ShouldReturnBadRequest_WhenPaymentFails() {
        PaymentRequest paymentRequest = new PaymentRequest()
                .userId("user123")
                .amount(new BigDecimal("2000.00"));

        PaymentResponse paymentResponse = new PaymentResponse(
                "user123",
                new BigDecimal("2000.00"),
                new BigDecimal("1500.00"),
                PaymentResponse.StatusEnum.FAILED,
                "Недостаточно средств для платежа"
        );

        when(paymentService.processPayment(any(PaymentRequest.class)))
                .thenReturn(Mono.just(paymentResponse));

        webTestClient.post()
                .uri("/payments/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(paymentRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.userId").isEqualTo("user123")
                .jsonPath("$.amount").isEqualTo(2000.00)
                .jsonPath("$.remainingBalance").isEqualTo(1500.00)
                .jsonPath("$.status").isEqualTo("FAILED")
                .jsonPath("$.message").isEqualTo("Недостаточно средств для платежа");
    }

    @Test
    void processPayment_ShouldReturnInternalServerError_WhenServiceThrowsException() {
        PaymentRequest paymentRequest = new PaymentRequest()
                .userId("user123")
                .amount(new BigDecimal("100.00"));

        when(paymentService.processPayment(any(PaymentRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("Service error")));

        webTestClient.post()
                .uri("/payments/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(paymentRequest)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void processPayment_ShouldReturnBadRequest_WhenInvalidRequestBody() {
        webTestClient.post()
                .uri("/payments/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"invalid\": \"json\"}")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void getBalance_ShouldReturnBadRequest_WhenUserIdIsEmpty() {
        webTestClient.get()
                .uri("/payments/balance/{userId}", "")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }
}
