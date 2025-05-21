package ru.mdemidkin.intershop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mdemidkin.intershop.model.CartItem;
import ru.mdemidkin.intershop.repository.CartRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartItemRepository;

    @Transactional(readOnly = true)
    public Optional<CartItem> findItemById(Long itemId) {
        return cartItemRepository.findByItemId(itemId);
    }

    @Transactional
    public void saveOrUpdate(CartItem cartItem) {
        cartItemRepository.save(cartItem);
    }

    @Transactional
    public void delete(CartItem cartItem) {
        cartItemRepository.delete(cartItem);
    }

    @Transactional(readOnly = true)
    public List<CartItem> getAll() {
        return cartItemRepository.findAll();
    }

    @Transactional
    public void clearCart() {
        cartItemRepository.deleteAll();
    }


}
