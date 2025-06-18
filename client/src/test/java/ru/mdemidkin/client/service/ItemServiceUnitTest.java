package ru.mdemidkin.client.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mdemidkin.client.dto.ItemsSortedSearchPageDto;
import ru.mdemidkin.client.model.CartItem;
import ru.mdemidkin.client.model.Item;
import ru.mdemidkin.client.model.enums.ItemAction;
import ru.mdemidkin.client.model.enums.SortType;
import ru.mdemidkin.client.repository.ItemRepository;
import ru.mdemidkin.client.security.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceUnitTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CartService cartService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ItemService itemService;

    @Test
    void searchItems_shouldReturnDtoForAuthenticatedUser() {
        String username = "testuser";
        Long userId = 1L;

        User user = User.builder().id(userId).username(username).build();
        Item item = Item.builder().id(1L).title("Test").price(10.0).build();
        CartItem cartItem = CartItem.builder().itemId(1L).userId(userId).quantity(2).build();

        when(userService.findByUsername(username)).thenReturn(Mono.just(user));
        when(itemRepository.getItemsBySearch("", SortType.NO, 1, 10)).thenReturn(Flux.just(item));
        when(cartService.findCartItem(1L, userId)).thenReturn(Mono.just(cartItem));
        when(itemRepository.getCountBySearch("")).thenReturn(Mono.just(1L));

        Mono<ItemsSortedSearchPageDto> result = itemService.searchItems("", SortType.NO, 1, 10, username);

        ItemsSortedSearchPageDto dto = result.block();
        assertNotNull(dto);
        assertEquals(1, dto.responsePagingDto().pageNumber());
        assertEquals(1, dto.itemsTile().size());
    }

    @Test
    void searchItems_shouldReturnDtoForAnonymousUser() {
        Item item = Item.builder().id(1L).title("Test").price(10.0).build();

        when(itemRepository.getItemsBySearch("", SortType.NO, 1, 10)).thenReturn(Flux.just(item));
        when(itemRepository.getCountBySearch("")).thenReturn(Mono.just(1L));

        Mono<ItemsSortedSearchPageDto> result = itemService.searchItems("", SortType.NO, 1, 10, "");

        ItemsSortedSearchPageDto dto = result.block();
        assertNotNull(dto);
        assertEquals(1, dto.responsePagingDto().pageNumber());
        assertEquals(1, dto.itemsTile().size());
        assertEquals(0, dto.itemsTile().get(0).get(0).getCount());
    }

    @Test
    void getById_shouldReturnItemWithQuantity() {
        String username = "testuser";
        Long userId = 1L;

        User user = User.builder().id(userId).username(username).build();
        Item item = Item.builder().id(1L).title("Test").price(10.0).build();
        CartItem cartItem = CartItem.builder().itemId(1L).quantity(2).build();

        when(userService.findByUsername(username)).thenReturn(Mono.just(user));
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));
        when(cartService.findCartItem(1L, userId)).thenReturn(Mono.just(cartItem));

        Item result = itemService.getById(1L, username).block();
        assertNotNull(result);
        assertEquals(2, result.getCount());
    }

    @Test
    void getById_shouldReturnItemWithZeroQuantityForAnonymousUser() {
        Item item = Item.builder().id(1L).title("Test").price(10.0).build();

        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));

        Item result = itemService.getById(1L, "").block();
        assertNotNull(result);
        assertEquals(0, result.getCount());
    }

    @Test
    void updateCartItem_plus_shouldIncrementQuantity() {
        String username = "testuser";
        Long userId = 1L;

        User user = User.builder().id(userId).username(username).build();
        Item item = Item.builder().id(2L).title("Test").price(10.0).build();

        when(userService.findByUsername(username)).thenReturn(Mono.just(user));
        when(cartService.findCartItem(2L, userId)).thenReturn(Mono.empty());
        when(itemRepository.findById(2L)).thenReturn(Mono.just(item));
        when(cartService.saveOrUpdate(any())).thenAnswer(invocation -> {
            CartItem cartItem = invocation.getArgument(0);
            return Mono.just(cartItem);
        });

        CartItem result = itemService.updateCartItem(2L, ItemAction.plus, username).block();
        assertNotNull(result);
        assertEquals(1, result.getQuantity());
        assertEquals(2L, result.getItemId());
        assertEquals(userId, result.getUserId());
    }

    @Test
    void updateCartItem_minus_shouldDeleteIfQuantityIsOne() {
        String username = "testuser";
        Long userId = 1L;

        User user = User.builder().id(userId).username(username).build();
        CartItem cartItem = CartItem.builder().id(1L).itemId(2L).userId(userId).quantity(1).build();

        when(userService.findByUsername(username)).thenReturn(Mono.just(user));
        when(cartService.findCartItem(2L, userId)).thenReturn(Mono.just(cartItem));
        when(cartService.delete(cartItem)).thenReturn(Mono.empty());

        CartItem result = itemService.updateCartItem(2L, ItemAction.minus, username).block();
        assertNull(result);
    }

    @Test
    void updateCartItem_delete_shouldCallDelete() {
        String username = "testuser";
        Long userId = 1L;

        User user = User.builder().id(userId).username(username).build();
        CartItem cartItem = CartItem.builder().id(1L).itemId(2L).userId(userId).quantity(5).build();

        when(userService.findByUsername(username)).thenReturn(Mono.just(user));
        when(cartService.findCartItem(2L, userId)).thenReturn(Mono.just(cartItem));
        when(cartService.delete(cartItem)).thenReturn(Mono.empty());

        CartItem result = itemService.updateCartItem(2L, ItemAction.delete, username).block();
        assertNull(result);
    }

    @Test
    void getByOrderId_shouldReturnItemsList() {
        Item item = Item.builder().id(1L).title("Item 1").build();
        when(itemRepository.findItemsByOrderId(1L)).thenReturn(Flux.just(item));

        List<Item> items = itemService.getByOrderId(1L).block();
        assertNotNull(items);
        assertEquals(1, items.size());
    }
}
