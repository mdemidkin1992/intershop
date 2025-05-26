package ru.mdemidkin.intershop.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mdemidkin.intershop.dto.CartItemListDto;
import ru.mdemidkin.intershop.dto.OrderDto;
import ru.mdemidkin.intershop.mapper.OrderMapper;
import ru.mdemidkin.intershop.model.Item;
import ru.mdemidkin.intershop.model.Order;
import ru.mdemidkin.intershop.model.OrderItem;
import ru.mdemidkin.intershop.repository.OrderItemRepository;
import ru.mdemidkin.intershop.repository.OrderRepository;

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

    @InjectMocks
    private OrderService orderService;

    @Test
    void findAll_shouldReturnOrderDtos() {
        Order order = new Order();
        order.setId(1L);
        List<Item> items = List.of(Item.builder().id(1L).build());
        OrderDto dto = Mockito.mock(OrderDto.class);

        when(orderRepository.findAll()).thenReturn(Flux.just(order));
        when(itemService.getByOrderId(1L)).thenReturn(Mono.just(items));
        when(mapper.toDto(order, items)).thenReturn(dto);

        List<OrderDto> result = orderService.findAll().collectList().block();
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
        Item item = Item.builder().id(1L).count(2).price(10.0).build();
        OrderItem orderItem = OrderItem.builder().itemId(1L).quantity(2).pricePerItem(10.0).build();
        Order order = new Order();
        order.setId(1L);

        when(itemService.getCartItemListDto())
                .thenReturn(Mono.just(new CartItemListDto(List.of(item), 20.0, false)));
        when(orderRepository.save(any())).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            saved.setId(1L);
            return Mono.just(saved);
        });
        when(orderItemRepository.saveAll(any(Iterable.class))).thenReturn(Flux.just(orderItem));
        when(cartService.clearCart()).thenReturn(Mono.empty());

        Order result = orderService.createOrder().block();
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(20.0, result.getTotalPrice());
    }
}
