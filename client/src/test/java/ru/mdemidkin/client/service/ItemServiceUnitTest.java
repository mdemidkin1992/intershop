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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceUnitTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private ItemService itemService;

    @Test
    void searchItems_shouldReturnDto() {
        Item item = Item.builder().id(1L).title("Test").price(10.0).build();
        CartItem cartItem = CartItem.builder().itemId(1L).quantity(2).build();

        when(itemRepository.getItemsBySearch("", SortType.NO, 1, 10)).thenReturn(Flux.just(item));
        when(cartService.findItemById(1L)).thenReturn(Mono.just(cartItem));
        when(itemRepository.getCountBySearch(""))
                .thenReturn(Mono.just(1L));

        Mono<ItemsSortedSearchPageDto> result = itemService.searchItems("", SortType.NO, 1, 10);

        ItemsSortedSearchPageDto dto = result.block();
        assertNotNull(dto);
        assertEquals(1, dto.responsePagingDto().pageNumber());
        assertEquals(1, dto.itemsTile().size());
    }

    @Test
    void getById_shouldReturnItemWithQuantity() {
        Item item = Item.builder().id(1L).title("Test").price(10.0).build();
        CartItem cartItem = CartItem.builder().itemId(1L).quantity(2).build();

        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));
        when(cartService.findItemById(1L)).thenReturn(Mono.just(cartItem));

        Item result = itemService.getById(1L).block();
        assertNotNull(result);
        assertEquals(2, result.getCount());
    }

    @Test
    void updateCartItem_plus_shouldIncrementQuantity() {
        Item mock = mock(Item.class);
        CartItem cartItem = CartItem.builder().id(1L).itemId(2L).quantity(1).build();

        when(cartService.findItemById(2L)).thenReturn(Mono.just(cartItem));
        when(cartService.saveOrUpdate(any())).thenReturn(Mono.just(cartItem));
        when(itemRepository.findById(anyLong())).thenReturn(Mono.just(mock));

        CartItem result = itemService.updateCartItem(2L, ItemAction.plus).block();
        assertNotNull(result);
        assertEquals(2, result.getQuantity());
    }

    @Test
    void updateCartItem_minus_shouldDeleteIfQuantityIsOne() {
        CartItem cartItem = CartItem.builder().id(1L).itemId(2L).quantity(1).build();

        when(cartService.findItemById(2L)).thenReturn(Mono.just(cartItem));
        when(cartService.delete(cartItem)).thenReturn(Mono.empty());

        CartItem result = itemService.updateCartItem(2L, ItemAction.minus).block();
        assertNull(result);
    }

    @Test
    void updateCartItem_delete_shouldCallDelete() {
        CartItem cartItem = CartItem.builder().id(1L).itemId(2L).quantity(5).build();

        when(cartService.findItemById(2L)).thenReturn(Mono.just(cartItem));
        when(cartService.delete(cartItem)).thenReturn(Mono.empty());

        CartItem result = itemService.updateCartItem(2L, ItemAction.delete).block();
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
