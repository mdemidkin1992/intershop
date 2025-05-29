package ru.mdemidkin.intershop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mdemidkin.intershop.model.CartItem;
import ru.mdemidkin.intershop.repository.CartRepository;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartItemRepository;

    public Mono<CartItem> findItemById(Long itemId) {
        return cartItemRepository.findByItemId(itemId);
    }

    public Mono<CartItem> saveOrUpdate(CartItem cartItem) {
        return cartItemRepository.save(cartItem);
    }

    public Mono<Void> delete(CartItem cartItem) {
        return cartItemRepository.delete(cartItem);
    }

    public Flux<CartItem> getAll() {
        return cartItemRepository.findAll();
    }

    public Mono<Void> clearCart() {
        return cartItemRepository.deleteAll();
    }

}
