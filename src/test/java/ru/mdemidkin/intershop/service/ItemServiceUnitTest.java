package ru.mdemidkin.intershop.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.mdemidkin.intershop.dto.CartItemListDto;
import ru.mdemidkin.intershop.dto.ItemsSortedSearchPageDto;
import ru.mdemidkin.intershop.model.CartItem;
import ru.mdemidkin.intershop.model.Item;
import ru.mdemidkin.intershop.model.enums.ItemAction;
import ru.mdemidkin.intershop.model.enums.SortType;
import ru.mdemidkin.intershop.repository.ItemRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceUnitTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private ItemService itemService;

    private Item item1;
    private Item item2;
    private Item item3;
    private List<Item> itemList;
    private Page<Item> itemPage;
    private CartItem cartItem1;

    @BeforeEach
    void setUp() {
        item1 = new Item();
        item1.setId(1L);
        item1.setTitle("Test Item 1");
        item1.setDescription("Description 1");
        item1.setPrice(10.0);

        item2 = new Item();
        item2.setId(2L);
        item2.setTitle("Test Item 2");
        item2.setDescription("Description 2");
        item2.setPrice(20.0);

        item3 = new Item();
        item3.setId(3L);
        item3.setTitle("Test Item 3");
        item3.setDescription("Description 3");
        item3.setPrice(30.0);

        itemList = Arrays.asList(item1, item2, item3);
        itemPage = new PageImpl<>(itemList);

        cartItem1 = new CartItem();
        cartItem1.setId(1L);
        cartItem1.setItem(item1);
        cartItem1.setQuantity(2);
    }

    @Test
    void searchItems_withEmptySearch_shouldReturnAllItems() {
        when(itemRepository.findAll(any(Pageable.class))).thenReturn(itemPage);
        when(cartService.findItemById(anyLong())).thenReturn(Optional.empty());

        ItemsSortedSearchPageDto result = itemService.searchItems("", SortType.NO, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.itemsTile().size());
        assertEquals(3, result.itemsTile().get(0).size());
        assertEquals("Test Item 1", result.itemsTile().get(0).get(0).getTitle());
        assertEquals(SortType.NO, result.sortType());
        assertEquals(1, result.responsePagingDto().pageNumber());
        verify(itemRepository).findAll(any(Pageable.class));
        verify(cartService, times(3)).findItemById(anyLong());
    }

    @Test
    void searchItems_withSearchTerm_shouldReturnFilteredItems() {
        when(itemRepository.findByTitleOrDescriptionContaining(eq("Test"), any(Pageable.class))).thenReturn(itemPage);
        when(cartService.findItemById(anyLong())).thenReturn(Optional.empty());

        ItemsSortedSearchPageDto result = itemService.searchItems("Test", SortType.ALPHA, 1, 10);

        assertNotNull(result);
        assertEquals("Test", result.search());
        assertEquals(SortType.ALPHA, result.sortType());
        verify(itemRepository).findByTitleOrDescriptionContaining(eq("Test"), any(Pageable.class));
    }

    @Test
    void searchItems_withAlphaSort_shouldUseTitleSorting() {
        when(itemRepository.findAll(any(Pageable.class))).thenReturn(itemPage);
        when(cartService.findItemById(anyLong())).thenReturn(Optional.empty());

        itemService.searchItems("", SortType.ALPHA, 1, 10);

        verify(itemRepository).findAll(any(Pageable.class));
    }

    @Test
    void searchItems_withPriceSort_shouldUsePriceSorting() {
        when(itemRepository.findAll(any(Pageable.class))).thenReturn(itemPage);
        when(cartService.findItemById(anyLong())).thenReturn(Optional.empty());

        itemService.searchItems("", SortType.PRICE, 1, 10);

        verify(itemRepository).findAll(any(Pageable.class));
    }

    @Test
    void getById_withExistingId_shouldReturnItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(cartService.findItemById(1L)).thenReturn(Optional.of(cartItem1));

        Item result = itemService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Item 1", result.getTitle());
        assertEquals(2, result.getCount());
        verify(itemRepository).findById(1L);
        verify(cartService).findItemById(1L);
    }

    @Test
    void getById_withNonExistingId_shouldThrowException() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.getById(99L));
        verify(itemRepository).findById(99L);
    }

    @Test
    void updateCartItem_withActionPlusAndNewItem_shouldCreateCartItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(cartService.findItemById(1L)).thenReturn(Optional.empty());

        itemService.updateCartItem(1L, ItemAction.plus);

        verify(itemRepository).findById(1L);
        verify(cartService).findItemById(1L);
        verify(cartService).saveOrUpdate(argThat(cartItem ->
                cartItem.getItem().equals(item1) && cartItem.getQuantity() == 1
        ));
    }

    @Test
    void updateCartItem_withActionPlusAndExistingItem_shouldIncrementQuantity() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(cartService.findItemById(1L)).thenReturn(Optional.of(cartItem1));

        itemService.updateCartItem(1L, ItemAction.plus);

        verify(cartService).saveOrUpdate(argThat(cartItem ->
                cartItem.getQuantity() == 3
        ));
    }

    @Test
    void updateCartItem_withActionMinusAndQuantityGreaterThanOne_shouldDecrementQuantity() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(cartService.findItemById(1L)).thenReturn(Optional.of(cartItem1));

        itemService.updateCartItem(1L, ItemAction.minus);

        verify(cartService).saveOrUpdate(argThat(cartItem ->
                cartItem.getQuantity() == 1
        ));
    }

    @Test
    void updateCartItem_withActionMinusAndQuantityOne_shouldDeleteCartItem() {
        CartItem cartItemWithOneQuantity = new CartItem();
        cartItemWithOneQuantity.setId(1L);
        cartItemWithOneQuantity.setItem(item1);
        cartItemWithOneQuantity.setQuantity(1);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(cartService.findItemById(1L)).thenReturn(Optional.of(cartItemWithOneQuantity));

        itemService.updateCartItem(1L, ItemAction.minus);

        verify(cartService).delete(cartItemWithOneQuantity);
    }

    @Test
    void updateCartItem_withActionDelete_shouldDeleteCartItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(cartService.findItemById(1L)).thenReturn(Optional.of(cartItem1));

        itemService.updateCartItem(1L, ItemAction.delete);

        verify(cartService).delete(cartItem1);
    }

    @Test
    void getCartItemListDto_withItemsInCart_shouldReturnCorrectDto() {
        List<CartItem> cartItems = new ArrayList<>();
        cartItems.add(cartItem1);

        CartItem cartItem2 = new CartItem();
        cartItem2.setId(2L);
        cartItem2.setItem(item2);
        cartItem2.setQuantity(3);
        cartItems.add(cartItem2);

        when(cartService.getAll()).thenReturn(cartItems);

        CartItemListDto result = itemService.getCartItemListDto();

        assertNotNull(result);
        assertEquals(2, result.items().size());
        assertEquals(1L, result.items().get(0).getId());
        assertEquals(2, result.items().get(0).getCount());
        assertEquals(2L, result.items().get(1).getId());
        assertEquals(3, result.items().get(1).getCount());
        assertEquals(80.0, result.cartTotal());
        assertFalse(result.isCartEmpty());
        verify(cartService).getAll();
    }

    @Test
    void getCartItemListDto_withEmptyCart_shouldReturnEmptyDto() {
        when(cartService.getAll()).thenReturn(new ArrayList<>());

        CartItemListDto result = itemService.getCartItemListDto();

        assertNotNull(result);
        assertTrue(result.items().isEmpty());
        assertEquals(0.0, result.cartTotal());
        assertTrue(result.isCartEmpty());
        verify(cartService).getAll();
    }
}
