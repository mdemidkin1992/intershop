package ru.mdemidkin.intershop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemService itemService;
    private final CartService cartService;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper mapper;

    public Flux<OrderDto> findAll() {
        return orderRepository.findAll()
                .flatMap(order -> itemService.getByOrderId(order.getId())
                        .map(items -> mapper.toDto(order, items)));
    }

    public Mono<OrderDto> findById(Long id) {
        return Mono.zip(orderRepository.findById(id),
                        itemService.getByOrderId(id))
                .map(t -> mapper.toDto(t.getT1(), t.getT2()));
    }

    public Mono<Order> createOrder() {
        return itemService.getCartItemListDto()
                .map(CartItemListDto::items)
                .flatMap(this::buildOrderWithItems)
                .flatMap(this::saveOrderWithItems)
                .flatMap(this::clearCart);
    }

    private Mono<Order> buildOrderWithItems(List<Item> items) {
        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        order.setOrderItems(new ArrayList<>());

        double totalPrice = 0.0;

        for (Item item : items) {
            OrderItem orderItem = OrderItem.builder()
                    .itemId(item.getId())
                    .quantity(item.getCount())
                    .pricePerItem(item.getPrice())
                    .build();

            order.getOrderItems().add(orderItem);
            totalPrice += item.getPrice() * item.getCount();
        }

        order.setTotalPrice(totalPrice);
        return Mono.just(order);
    }

    private Mono<Order> saveOrderWithItems(Order order) {
        return orderRepository.save(order)
                .flatMap(savedOrder -> {
                    for (OrderItem item : order.getOrderItems()) {
                        item.setOrderId(savedOrder.getId());
                    }

                    return orderItemRepository.saveAll(order.getOrderItems())
                            .then(Mono.just(savedOrder));
                });
    }

    private Mono<Order> clearCart(Order order) {
        return cartService.clearCart().thenReturn(order);
    }

}
