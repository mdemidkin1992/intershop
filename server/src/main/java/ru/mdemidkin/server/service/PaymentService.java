package ru.mdemidkin.server.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.mdemidkin.intershop.server.domain.BalanceResponse;
import ru.mdemidkin.intershop.server.domain.PaymentRequest;
import ru.mdemidkin.intershop.server.domain.PaymentResponse;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PaymentService {

    private final ConcurrentHashMap<String, BigDecimal> userBalances = new ConcurrentHashMap<>();

    public PaymentService() {
        userBalances.put("user1", new BigDecimal("1500.00"));
        userBalances.put("user2", new BigDecimal("2300.50"));
        userBalances.put("user3", new BigDecimal("500.75"));
    }

    /**
     * Получение баланса пользователя
     * Логика: если пользователь существует - возвращаем его баланс,
     * иначе генерируем случайный баланс от 0 до 3000
     */
    public Mono<BalanceResponse> getBalance(String userId) {
        return Mono.fromCallable(() -> {
                    BigDecimal balance = userBalances.computeIfAbsent(userId,
                            k -> BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(0, 3000))
                                    .setScale(2, BigDecimal.ROUND_HALF_UP));

                    return new BalanceResponse(userId, balance);
                });
    }

    /**
     * Осуществление платежа
     * Проверяет достаточность средств и списывает сумму
     */
    public Mono<PaymentResponse> processPayment(PaymentRequest request) {
        return Mono.fromCallable(() -> {
                    BigDecimal currentBalance = userBalances.get(request.getUserId());

                    if (currentBalance == null) {
                        return new PaymentResponse(
                                request.getUserId(),
                                request.getAmount(),
                                BigDecimal.ZERO,
                                PaymentResponse.StatusEnum.FAILED,
                                "Не найден пользователь по id " + request.getUserId()
                        );
                    }

                    if (currentBalance.compareTo(request.getAmount()) < 0) {
                        return new PaymentResponse(
                                request.getUserId(),
                                request.getAmount(),
                                currentBalance,
                                PaymentResponse.StatusEnum.FAILED,
                                "Недостаточно средств для платежа"
                        );
                    }

                    BigDecimal newBalance = currentBalance.subtract(request.getAmount());
                    userBalances.put(request.getUserId(), newBalance);

                    return new PaymentResponse(
                            request.getUserId(),
                            request.getAmount(),
                            newBalance,
                            PaymentResponse.StatusEnum.SUCCESS,
                            "Платеж успешно обработан"
                    );
                });
    }
}
