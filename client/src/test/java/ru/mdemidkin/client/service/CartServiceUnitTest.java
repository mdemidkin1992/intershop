package ru.mdemidkin.client.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mdemidkin.client.model.CartItem;
import ru.mdemidkin.client.repository.CartRepository;
import ru.mdemidkin.client.security.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceUnitTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CartService cartService;

    @Test
    void findItemById_shouldReturnCartItem() {
        CartItem item = CartItem.builder().id(1L).itemId(2L).quantity(3).userId(1L).build();
        when(cartRepository.findByItemIdAndUserId(2L, 1L)).thenReturn(Mono.just(item));

        cartService.findCartItem(2L, 1L)
                .doOnNext(found -> {
                    assertEquals(1L, found.getUserId());
                    assertEquals(2L, found.getItemId());
                    assertEquals(3, found.getQuantity());
                })
                .block();
    }

    @Test
    void saveOrUpdate_shouldSaveItem() {
        CartItem item = CartItem.builder().itemId(2L).quantity(1).userId(1L).build();
        when(cartRepository.save(item)).thenReturn(Mono.just(item));

        CartItem saved = cartService.saveOrUpdate(item).block();
        assertNotNull(saved);
        assertEquals(2L, saved.getItemId());
        assertEquals(1L, saved.getUserId());
    }

    @Test
    void delete_shouldCompleteSuccessfully() {
        CartItem item = CartItem.builder().id(1L).itemId(2L).userId(1L).build();
        when(cartRepository.delete(item)).thenReturn(Mono.empty());

        Assertions.assertDoesNotThrow(() -> cartService.delete(item).block());
    }

    @Test
    void getAll_shouldReturnAllItems() {
        String username = "user";
        Long userId = 1L;

        User user = User.builder()
                .id(userId)
                .username(username)
                .build();

        CartItem a = CartItem.builder().id(1L).itemId(2L).userId(userId).build();
        CartItem b = CartItem.builder().id(2L).itemId(3L).userId(userId).build();

        when(userService.findByUsername(username)).thenReturn(Mono.just(user));
        when(cartRepository.findAllByUserId(userId)).thenReturn(Flux.just(a, b));

        var list = cartService.getAll(username).collectList().block();
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(userId, list.get(0).getUserId());
        assertEquals(userId, list.get(1).getUserId());
    }

    @Test
    void clearCart_shouldCompleteSuccessfully() {
        Long userId = 1L;
        when(cartRepository.deleteAllByUserId(userId)).thenReturn(Mono.empty());
        Assertions.assertDoesNotThrow(() -> cartService.clearCart(userId).block());
    }
}
