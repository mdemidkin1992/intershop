package ru.mdemidkin.client.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mdemidkin.client.dto.CartItemListDto;
import ru.mdemidkin.client.dto.OrderDto;
import ru.mdemidkin.client.mapper.OrderMapper;
import ru.mdemidkin.client.model.Item;
import ru.mdemidkin.client.model.Order;
import ru.mdemidkin.client.model.OrderItem;
import ru.mdemidkin.client.repository.OrderItemRepository;
import ru.mdemidkin.client.repository.OrderRepository;
import ru.mdemidkin.client.security.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemService itemService;

    @Mock
    private CartService cartService;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderMapper mapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void findAll_shouldReturnOrderDtos() {
        String username = "testuser";
        Long userId = 1L;

        User user = User.builder().id(userId).username(username).build();
        Order order = new Order();
        order.setId(1L);
        order.setUserId(userId);

        List<Item> items = List.of(Item.builder().id(1L).build());
        OrderDto dto = Mockito.mock(OrderDto.class);

        when(userService.findByUsername(username)).thenReturn(Mono.just(user));
        when(orderRepository.findAllByUserId(userId)).thenReturn(Flux.just(order));
        when(itemService.getByOrderId(1L)).thenReturn(Mono.just(items));
        when(mapper.toDto(order, items)).thenReturn(dto);

        List<OrderDto> result = orderService.findAll(username).collectList().block();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void findById_shouldReturnOrderDto() {
        Order order = new Order();
        order.setId(1L);
        List<Item> items = List.of(Item.builder().id(1L).build());
        OrderDto dto = Mockito.mock(OrderDto.class);

        when(orderRepository.findById(1L)).thenReturn(Mono.just(order));
        when(itemService.getByOrderId(1L)).thenReturn(Mono.just(items));
        when(mapper.toDto(order, items)).thenReturn(dto);

        OrderDto result = orderService.findById(1L).block();
        assertNotNull(result);
    }

    @Test
    void createOrder_shouldBuildSaveAndClear() {
        String username = "testuser";
        Long userId = 1L;

        User user = User.builder().id(userId).username(username).build();
        Item item = Item.builder().id(1L).count(2).price(10.0).build();
        OrderItem orderItem = OrderItem.builder().itemId(1L).quantity(2).pricePerItem(10.0).build();
        Order order = new Order();
        order.setId(1L);

        when(userService.findByUsername(username)).thenReturn(Mono.just(user));
        when(itemService.getCartItemListDto(username))
                .thenReturn(Mono.just(new CartItemListDto(List.of(item), 20.0, false)));
        when(orderRepository.save(any())).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            saved.setId(1L);
            return Mono.just(saved);
        });
        when(orderItemRepository.saveAll(any(Iterable.class))).thenReturn(Flux.just(orderItem));
        when(cartService.clearCart(userId)).thenReturn(Mono.empty());

        Order result = orderService.createOrder(username).block();
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(20.0, result.getTotalPrice());
    }
}