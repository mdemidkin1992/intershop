package ru.mdemidkin.client.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
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

    @Cacheable(cacheNames = "orders")
    public Flux<OrderDto> findAll() {
        return orderRepository.findAll()
                .flatMap(order -> itemService.getByOrderId(order.getId())
                        .map(items -> mapper.toDto(order, items)));
    }

    @Cacheable(cacheNames = "order", key = "#id")
    public Mono<OrderDto> findById(Long id) {
        return Mono.zip(orderRepository.findById(id),
                        itemService.getByOrderId(id))
                .map(t -> mapper.toDto(t.getT1(), t.getT2()));
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = {"cartItems"}, allEntries = true),
            @CacheEvict(cacheNames = {"searchItems"}, allEntries = true)})
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


        for (Item item : items) {
            OrderItem orderItem = OrderItem.builder()
                    .itemId(item.getId())
                    .quantity(item.getCount())
                    .pricePerItem(item.getPrice())
                    .build();

            order.getOrderItems().add(orderItem);
        }

        order.setTotalPrice(calculateTotalPrice(items));
        return Mono.just(order);
    }

    public double calculateTotalPrice(List<Item> items) {
        return items.stream()
                .mapToDouble(item -> item.getPrice() * item.getCount())
                .sum();
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
