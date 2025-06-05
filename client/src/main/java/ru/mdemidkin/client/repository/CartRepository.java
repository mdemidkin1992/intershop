package ru.mdemidkin.client.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.mdemidkin.client.model.CartItem;

@Repository
public interface CartRepository extends R2dbcRepository<CartItem, Long> {

    Mono<CartItem> findByItemId(Long itemId);
}
