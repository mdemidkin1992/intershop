package ru.mdemidkin.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mdemidkin.intershop.server.domain.BalanceResponse;
import ru.mdemidkin.intershop.server.domain.PaymentRequest;
import ru.mdemidkin.intershop.server.domain.PaymentResponse;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    private ConcurrentHashMap<String, BigDecimal> userBalances;

    @BeforeEach
    void setUp() throws Exception {
        Field field = PaymentService.class.getDeclaredField("userBalances");
        field.setAccessible(true);
        userBalances = (ConcurrentHashMap<String, BigDecimal>) field.get(paymentService);
    }

    @Test
    void getBalance_ShouldReturnExistingBalance_WhenUserExists() {
        String userId = "user1";
        BigDecimal expectedBalance = new BigDecimal("1500.00");

        BalanceResponse result = paymentService.getBalance(userId).block();

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getBalance()).isEqualTo(expectedBalance);
    }

    @Test
    void getBalance_ShouldGenerateRandomBalance_WhenUserDoesNotExist() {
        String userId = "newUser";

        BalanceResponse result = paymentService.getBalance(userId).block();

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getBalance()).isNotNull();
        assertThat(result.getBalance()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(result.getBalance()).isLessThanOrEqualTo(new BigDecimal("3000.00"));
        assertThat(userBalances).containsKey(userId);
    }

    @Test
    void processPayment_ShouldReturnSuccess_WhenSufficientBalance() {
        String userId = "user1";
        BigDecimal paymentAmount = new BigDecimal("500.00");
        BigDecimal expectedNewBalance = new BigDecimal("1000.00");

        PaymentRequest request = new PaymentRequest()
                .userId(userId)
                .amount(paymentAmount);

        PaymentResponse result = paymentService.processPayment(request).block();

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getAmount()).isEqualTo(paymentAmount);
        assertThat(result.getRemainingBalance()).isEqualTo(expectedNewBalance);
        assertThat(result.getStatus()).isEqualTo(PaymentResponse.StatusEnum.SUCCESS);
        assertThat(result.getMessage()).isEqualTo("Платеж успешно обработан");
        assertThat(userBalances.get(userId)).isEqualTo(expectedNewBalance);
    }

    @Test
    void processPayment_ShouldReturnFailed_WhenInsufficientBalance() {
        String userId = "user3";
        BigDecimal paymentAmount = new BigDecimal("1000.00");
        BigDecimal currentBalance = new BigDecimal("500.75");

        PaymentRequest request = new PaymentRequest()
                .userId(userId)
                .amount(paymentAmount);

        PaymentResponse result = paymentService.processPayment(request).block();

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getAmount()).isEqualTo(paymentAmount);
        assertThat(result.getRemainingBalance()).isEqualTo(currentBalance);
        assertThat(result.getStatus()).isEqualTo(PaymentResponse.StatusEnum.FAILED);
        assertThat(result.getMessage()).isEqualTo("Недостаточно средств для платежа");
        assertThat(userBalances.get(userId)).isEqualTo(currentBalance);
    }

    @Test
    void processPayment_ShouldReturnFailed_WhenUserNotFound() {
        String userId = "nonExistentUser";
        BigDecimal paymentAmount = new BigDecimal("100.00");

        PaymentRequest request = new PaymentRequest()
                .userId(userId)
                .amount(paymentAmount);

        PaymentResponse result = paymentService.processPayment(request).block();

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getAmount()).isEqualTo(paymentAmount);
        assertThat(result.getRemainingBalance()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.getStatus()).isEqualTo(PaymentResponse.StatusEnum.FAILED);
        assertThat(result.getMessage()).isEqualTo("Не найден пользователь по id " + userId);
        assertThat(userBalances).doesNotContainKey(userId);
    }

    @Test
    void processPayment_ShouldHandleExactBalance_WhenPaymentEqualsBalance() {
        String userId = "user2";
        BigDecimal paymentAmount = new BigDecimal("2300.50");
        BigDecimal expectedNewBalance = new BigDecimal("0.00");

        PaymentRequest request = new PaymentRequest()
                .userId(userId)
                .amount(paymentAmount);

        PaymentResponse result = paymentService.processPayment(request).block();

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getAmount()).isEqualTo(paymentAmount);
        assertThat(result.getRemainingBalance()).isEqualTo(expectedNewBalance);
        assertThat(result.getStatus()).isEqualTo(PaymentResponse.StatusEnum.SUCCESS);
        assertThat(result.getMessage()).isEqualTo("Платеж успешно обработан");
        assertThat(userBalances.get(userId)).isEqualTo(expectedNewBalance);
    }

    @Test
    void processPayment_ShouldHandleZeroAmount() {
        String userId = "user1";
        BigDecimal paymentAmount = BigDecimal.ZERO;
        BigDecimal initialBalance = new BigDecimal("1500.00");

        PaymentRequest request = new PaymentRequest()
                .userId(userId)
                .amount(paymentAmount);

        PaymentResponse result = paymentService.processPayment(request).block();

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(PaymentResponse.StatusEnum.SUCCESS);
        assertThat(result.getRemainingBalance()).isEqualTo(initialBalance);
        assertThat(userBalances.get(userId)).isEqualTo(initialBalance);
    }

    @Test
    void getBalance_ShouldGenerateDifferentBalances_ForDifferentNewUsers() {
        String userId1 = "newUser1";
        String userId2 = "newUser2";

        BalanceResponse result1 = paymentService.getBalance(userId1).block();
        BalanceResponse result2 = paymentService.getBalance(userId2).block();

        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result1.getUserId()).isEqualTo(userId1);
        assertThat(result2.getUserId()).isEqualTo(userId2);

        assertThat(result1.getBalance()).isBetween(BigDecimal.ZERO, new BigDecimal("3000.00"));
        assertThat(result2.getBalance()).isBetween(BigDecimal.ZERO, new BigDecimal("3000.00"));

        assertThat(userBalances).containsKeys(userId1, userId2);
    }
}
