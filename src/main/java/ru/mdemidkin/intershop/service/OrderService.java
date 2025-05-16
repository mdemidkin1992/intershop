package ru.mdemidkin.intershop.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mdemidkin.intershop.model.Item;
import ru.mdemidkin.intershop.model.Order;
import ru.mdemidkin.intershop.model.OrderItem;
import ru.mdemidkin.intershop.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemService itemService;
    private final CartService cartService;

    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
    }

    @Transactional
    public Order createOrder() {
        double totalPrice = 0.0;
        List<Item> cartItems = itemService.getCartItemListDto().items();
        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalPrice(totalPrice);

        Order savedOrder = orderRepository.save(order);

        for (Item item : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setItem(item);
            orderItem.setQuantity(item.getCount());
            orderItem.setPricePerItem(item.getPrice());

            savedOrder.getOrderItems().add(orderItem);

            totalPrice = totalPrice + (item.getPrice() * item.getCount());
        }

        savedOrder.setTotalPrice(totalPrice);

        cartService.clearCart();

        return orderRepository.save(savedOrder);
    }

}
