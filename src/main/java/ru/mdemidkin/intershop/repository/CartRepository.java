package ru.mdemidkin.intershop.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.mdemidkin.intershop.model.CartItem;

@Repository
public interface CartRepository extends R2dbcRepository<CartItem, Long> {

    Mono<CartItem> findByItemId(Long itemId);
}
