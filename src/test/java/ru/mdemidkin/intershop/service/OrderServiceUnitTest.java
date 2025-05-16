package ru.mdemidkin.intershop.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mdemidkin.intershop.dto.CartItemListDto;
import ru.mdemidkin.intershop.model.Item;
import ru.mdemidkin.intershop.model.Order;
import ru.mdemidkin.intershop.model.OrderItem;
import ru.mdemidkin.intershop.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemService itemService;

    @Mock
    private CartService cartService;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private List<Order> testOrders;
    private List<Item> testCartItems;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setCreatedAt(LocalDateTime.now());
        testOrder.setTotalPrice(100.0);
        testOrder.setOrderItems(new ArrayList<>());

        Order order2 = new Order();
        order2.setId(2L);
        order2.setCreatedAt(LocalDateTime.now());
        order2.setTotalPrice(200.0);
        order2.setOrderItems(new ArrayList<>());

        testOrders = Arrays.asList(testOrder, order2);

        Item item1 = new Item();
        item1.setId(1L);
        item1.setTitle("Item 1");
        item1.setPrice(10.0);
        item1.setCount(2);

        Item item2 = new Item();
        item2.setId(2L);
        item2.setTitle("Item 2");
        item2.setPrice(20.0);
        item2.setCount(4);

        testCartItems = Arrays.asList(item1, item2);
    }

    @Test
    void findAll_shouldReturnAllOrders() {
        when(orderRepository.findAll()).thenReturn(testOrders);

        List<Order> result = orderService.findAll();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        verify(orderRepository).findAll();
    }

    @Test
    void findById_withExistingId_shouldReturnOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        Order result = orderService.findById(1L);

        assertEquals(1L, result.getId());
        assertEquals(100.0, result.getTotalPrice());
        verify(orderRepository).findById(1L);
    }

    @Test
    void findById_withNonExistingId_shouldThrowException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> orderService.findById(99L));
        verify(orderRepository).findById(99L);
    }

    @Test
    void createOrder_shouldCreateAndSaveOrder() {
        when(itemService.getCartItemListDto()).thenReturn(new CartItemListDto(testCartItems, 100.0, false));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order orderToSave = invocation.getArgument(0);
            if (orderToSave.getId() == null) {
                orderToSave.setId(1L);
            }
            return orderToSave;
        });

        Order result = orderService.createOrder();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertNotNull(result.getCreatedAt());
        assertEquals(2, result.getOrderItems().size());
        assertEquals(100.0, result.getTotalPrice());

        verify(itemService).getCartItemListDto();
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(cartService).clearCart();
    }

    @Test
    void createOrder_shouldCalculateTotalPriceCorrectly() {
        when(itemService.getCartItemListDto()).thenReturn(new CartItemListDto(testCartItems, 100.0, false));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order orderToSave = invocation.getArgument(0);
            if (orderToSave.getId() == null) {
                orderToSave.setId(1L);
            }
            return orderToSave;
        });

        Order result = orderService.createOrder();

        assertEquals(100.0, result.getTotalPrice());
        for (OrderItem orderItem : result.getOrderItems()) {
            if (orderItem.getItem().getId() == 1L) {
                assertEquals(2, orderItem.getQuantity());
                assertEquals(10.0, orderItem.getPricePerItem());
            } else if (orderItem.getItem().getId() == 2L) {
                assertEquals(4, orderItem.getQuantity());
                assertEquals(20.0, orderItem.getPricePerItem());
            }
        }
    }
}
