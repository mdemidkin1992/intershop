package ru.mdemidkin.intershop.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mdemidkin.intershop.model.CartItem;
import ru.mdemidkin.intershop.repository.CartRepository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceUnitTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void findItemById_shouldReturnCartItem() {
        CartItem item = CartItem.builder().id(1L).itemId(2L).quantity(3).build();
        when(cartRepository.findByItemId(2L)).thenReturn(Mono.just(item));

        cartService.findItemById(2L)
                .doOnNext(found -> {
                    assertEquals(2L, found.getItemId());
                    assertEquals(3, found.getQuantity());
                })
                .block();
    }

    @Test
    void saveOrUpdate_shouldSaveItem() {
        CartItem item = CartItem.builder().itemId(2L).quantity(1).build();
        when(cartRepository.save(item)).thenReturn(Mono.just(item));

        CartItem saved = cartService.saveOrUpdate(item).block();
        assertNotNull(saved);
        assertEquals(2L, saved.getItemId());
    }

    @Test
    void delete_shouldCompleteSuccessfully() {
        CartItem item = CartItem.builder().id(1L).itemId(2L).build();
        when(cartRepository.delete(item)).thenReturn(Mono.empty());

        assertDoesNotThrow(() -> cartService.delete(item).block());
    }

    @Test
    void getAll_shouldReturnAllItems() {
        CartItem a = CartItem.builder().id(1L).itemId(2L).build();
        CartItem b = CartItem.builder().id(2L).itemId(3L).build();
        when(cartRepository.findAll()).thenReturn(Flux.just(a, b));

        var list = cartService.getAll().collectList().block();
        assertNotNull(list);
        assertEquals(2, list.size());
    }

    @Test
    void clearCart_shouldCompleteSuccessfully() {
        when(cartRepository.deleteAll()).thenReturn(Mono.empty());
        assertDoesNotThrow(() -> cartService.clearCart().block());
    }
}
