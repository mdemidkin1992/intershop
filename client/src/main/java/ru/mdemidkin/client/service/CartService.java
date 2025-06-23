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
    private final UserService userService;

    @Cacheable(cacheNames = "cartItem", key = "{#itemId, #userId}")
    public Mono<CartItem> findCartItem(Long itemId, Long userId) {
        return cartItemRepository.findByItemIdAndUserId(itemId, userId);
    }

    @CachePut(cacheNames = "cartItem", key = "{#cartItem.itemId, #cartItem.userId}")
    public Mono<CartItem> saveOrUpdate(CartItem cartItem) {
        return cartItemRepository.save(cartItem);
    }

    @CacheEvict(cacheNames = "cartItem", key = "{#cartItem.itemId, #cartItem.userId}")
    public Mono<Void> delete(CartItem cartItem) {
        return cartItemRepository.delete(cartItem);
    }

    public Flux<CartItem> getAll(String username) {
        return userService.findByUsername(username)
                .flatMapMany(user -> cartItemRepository.findAllByUserId(user.getId()));
    }

    @CacheEvict(cacheNames = {"cartItem"}, allEntries = true)
    public Mono<Void> clearCart(Long userId) {
        return cartItemRepository.deleteAllByUserId(userId);
    }

}
