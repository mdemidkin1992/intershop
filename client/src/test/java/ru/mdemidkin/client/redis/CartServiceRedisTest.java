package ru.mdemidkin.client.redis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mdemidkin.client.config.TestContainersConfig;
import ru.mdemidkin.client.model.CartItem;
import ru.mdemidkin.client.repository.CartRepository;
import ru.mdemidkin.client.service.CartService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class CartServiceRedisTest extends TestContainersConfig {

    @Autowired
    private CartService cartService;

    @MockitoBean
    private CartRepository cartRepository;

    @Test
    void findItemById_ShouldCacheResult() {
        Long itemId = 1L;
        CartItem item = new CartItem();
        item.setItemId(itemId);
        item.setQuantity(5);
        when(cartRepository.findByItemId(itemId)).thenReturn(Mono.just(item));

        CartItem first = cartService.findItemById(itemId).block();
        CartItem second = cartService.findItemById(itemId).block();

        verify(cartRepository, times(1)).findByItemId(itemId);
        assertNotNull(first);
        assertNotNull(second);
        assertThat(first.getQuantity()).isEqualTo(5);
        assertThat(second.getQuantity()).isEqualTo(5);
    }

    @Test
    void saveOrUpdate_ShouldUpdateCacheAndCallRepository() {
        CartItem item = new CartItem();
        item.setItemId(1L);
        item.setQuantity(10);
        when(cartRepository.save(item)).thenReturn(Mono.just(item));

        CartItem result = cartService.saveOrUpdate(item).block();

        verify(cartRepository, times(1)).save(item);
        assertThat(result.getQuantity()).isEqualTo(10);
    }

    @Test
    void delete_ShouldCallRepositoryAndEvictCache() {
        CartItem item = new CartItem();
        item.setItemId(1L);
        when(cartRepository.delete(item)).thenReturn(Mono.empty());

        cartService.delete(item).block();

        verify(cartRepository, times(1)).delete(item);
    }

    @Test
    void clearCart_ShouldCallRepositoryAndClearCache() {
        when(cartRepository.deleteAll()).thenReturn(Mono.empty());

        cartService.clearCart().block();

        verify(cartRepository, times(1)).deleteAll();
    }

    @Test
    void getAll_ShouldNotUseCache() {
        CartItem item1 = new CartItem();
        CartItem item2 = new CartItem();
        when(cartRepository.findAll()).thenReturn(Flux.just(item1, item2));

        cartService.getAll().collectList().block();
        cartService.getAll().collectList().block();

        verify(cartRepository, times(2)).findAll();
    }

    @Test
    void cacheEviction_ShouldWorkAfterDelete() {
        Long itemId = 1L;
        CartItem item = new CartItem();
        item.setItemId(itemId);
        when(cartRepository.findByItemId(itemId)).thenReturn(Mono.just(item));
        when(cartRepository.delete(item)).thenReturn(Mono.empty());

        cartService.findItemById(itemId).block();
        verify(cartRepository, times(1)).findByItemId(itemId);

        cartService.delete(item).block();

        cartService.findItemById(itemId).block();

        verify(cartRepository, times(2)).findByItemId(itemId);
    }
}
