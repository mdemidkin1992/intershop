package ru.mdemidkin.client.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mdemidkin.client.model.CartItem;
import ru.mdemidkin.client.repository.CartRepository;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartItemRepository;

    @Cacheable(cacheNames = "cartItem", key = "#itemId")
    public Mono<CartItem> findItemById(Long itemId) {
        return cartItemRepository.findByItemId(itemId);
    }

    @CachePut(cacheNames = "cartItem", key = "#cartItem.itemId")
    public Mono<CartItem> saveOrUpdate(CartItem cartItem) {
        return cartItemRepository.save(cartItem);
    }

    @CacheEvict(cacheNames = "cartItem", key = "#cartItem.itemId")
    public Mono<Void> delete(CartItem cartItem) {
        return cartItemRepository.delete(cartItem);
    }

    public Flux<CartItem> getAll() {
        return cartItemRepository.findAll();
    }

    @CacheEvict(cacheNames = {"cartItem"}, allEntries = true)
    public Mono<Void> clearCart() {
        return cartItemRepository.deleteAll();
    }

}
