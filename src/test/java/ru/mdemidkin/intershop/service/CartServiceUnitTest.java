//package ru.mdemidkin.intershop.service;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import ru.mdemidkin.intershop.model.CartItem;
//import ru.mdemidkin.intershop.repository.CartRepository;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//public class CartServiceUnitTest {
//
//    @Mock
//    private CartRepository cartItemRepository;
//
//    @InjectMocks
//    private CartService cartService;
//
//    private CartItem testCartItem;
//    private List<CartItem> testCartItems;
//
//    @BeforeEach
//    void setUp() {
//        testCartItem = new CartItem();
//        testCartItem.setId(1L);
//
//        CartItem item1 = new CartItem();
//        item1.setId(1L);
//
//        CartItem item2 = new CartItem();
//        item2.setId(2L);
//
//        testCartItems = Arrays.asList(item1, item2);
//    }
//
//    @Test
//    void findItemById_existingItem_returnsItem() {
//        when(cartItemRepository.findByItemId(1L)).thenReturn(Optional.of(testCartItem));
//
//        Optional<CartItem> result = cartService.findItemById(1L);
//
//        assertTrue(result.isPresent());
//        assertEquals(1L, result.get().getId());
//        verify(cartItemRepository).findByItemId(1L);
//    }
//
//    @Test
//    void findItemById_nonExistingItem_returnsEmptyOptional() {
//        when(cartItemRepository.findByItemId(99L)).thenReturn(Optional.empty());
//
//        Optional<CartItem> result = cartService.findItemById(99L);
//
//        assertFalse(result.isPresent());
//        verify(cartItemRepository).findByItemId(99L);
//    }
//
//    @Test
//    void saveOrUpdate_validItem_callsRepositorySave() {
//        cartService.saveOrUpdate(testCartItem);
//
//        verify(cartItemRepository).save(testCartItem);
//    }
//
//    @Test
//    void delete_existingItem_callsRepositoryDelete() {
//        cartService.delete(testCartItem);
//
//        verify(cartItemRepository).delete(testCartItem);
//    }
//
//    @Test
//    void getAll_returnsAllItems() {
//        when(cartItemRepository.findAll()).thenReturn(testCartItems);
//
//        List<CartItem> result = cartService.getAll();
//
//        assertEquals(2, result.size());
//        assertEquals(1L, result.get(0).getId());
//        assertEquals(2L, result.get(1).getId());
//        verify(cartItemRepository).findAll();
//    }
//
//    @Test
//    void clearCart_callsRepositoryDeleteAll() {
//        cartService.clearCart();
//
//        verify(cartItemRepository).deleteAll();
//    }
//}