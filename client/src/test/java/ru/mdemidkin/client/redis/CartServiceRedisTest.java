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
import ru.mdemidkin.client.security.User;
import ru.mdemidkin.client.service.CartService;
import ru.mdemidkin.client.service.UserService;

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

    @MockitoBean
    private UserService userService;

    @Test
    void findItemById_ShouldCacheResult() {
        Long userId = 10L;
        Long itemId = 1L;
        CartItem item = new CartItem();
        item.setItemId(itemId);
        item.setQuantity(5);
        item.setUserId(userId);
        when(cartRepository.findByItemIdAndUserId(itemId, userId)).thenReturn(Mono.just(item));

        CartItem first = cartService.findCartItem(itemId, userId).block();
        CartItem second = cartService.findCartItem(itemId, userId).block();

        verify(cartRepository, times(1)).findByItemIdAndUserId(itemId, userId);
        assertNotNull(first);
        assertNotNull(second);
        assertThat(first.getQuantity()).isEqualTo(5);
        assertThat(second.getQuantity()).isEqualTo(5);
    }

    @Test
    void saveOrUpdate_ShouldUpdateCacheAndCallRepository() {
        CartItem item = new CartItem();
        item.setItemId(1L);
        item.setUserId(10L);
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
        item.setUserId(10L);
        when(cartRepository.delete(item)).thenReturn(Mono.empty());

        cartService.delete(item).block();

        verify(cartRepository, times(1)).delete(item);
    }

    @Test
    void clearCart_ShouldCallRepositoryAndClearCache() {
        Long userId = 10L;
        when(cartRepository.deleteAllByUserId(userId)).thenReturn(Mono.empty());

        cartService.clearCart(userId).block();

        verify(cartRepository, times(1)).deleteAllByUserId(userId);
    }

    @Test
    void getAll_ShouldNotUseCache() {
        String username = "testuser";
        Long userId = 10L;
        User user = User.builder().id(userId).username(username).build();
        CartItem item1 = new CartItem();
        CartItem item2 = new CartItem();

        when(userService.findByUsername(username)).thenReturn(Mono.just(user));
        when(cartRepository.findAllByUserId(userId)).thenReturn(Flux.just(item1, item2));

        cartService.getAll(username).collectList().block();
        cartService.getAll(username).collectList().block();

        verify(cartRepository, times(2)).findAllByUserId(userId);
        verify(userService, times(2)).findByUsername(username);
    }

    @Test
    void cacheEviction_ShouldWorkAfterDelete() {
        Long itemId = 1L;
        Long userId = 10L;
        CartItem item = new CartItem();
        item.setItemId(itemId);
        item.setUserId(userId);
        when(cartRepository.findByItemIdAndUserId(itemId, userId)).thenReturn(Mono.just(item));
        when(cartRepository.delete(item)).thenReturn(Mono.empty());

        cartService.findCartItem(itemId, userId).block();
        verify(cartRepository, times(1)).findByItemIdAndUserId(itemId, userId);

        cartService.delete(item).block();

        cartService.findCartItem(itemId, userId).block();

        verify(cartRepository, times(2)).findByItemIdAndUserId(itemId, userId);
    }
}
