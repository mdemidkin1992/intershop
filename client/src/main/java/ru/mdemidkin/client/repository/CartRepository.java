package ru.mdemidkin.client.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mdemidkin.client.model.CartItem;

@Repository
public interface CartRepository extends R2dbcRepository<CartItem, Long> {

    Mono<CartItem> findByItemIdAndUserId(Long itemId, Long userId);

    Flux<CartItem> findAllByUserId(Long userId);

    Mono<Void> deleteAllByUserId(Long userId);
}
