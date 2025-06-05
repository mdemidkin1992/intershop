package ru.mdemidkin.client.mapper;

import org.springframework.stereotype.Component;
import ru.mdemidkin.client.dto.OrderDto;
import ru.mdemidkin.client.model.Item;
import ru.mdemidkin.client.model.Order;

import java.util.List;

@Component
public class OrderMapper {
    public OrderDto toDto(Order order, List<Item> items) {
        return new OrderDto(
                order.getId(),
                order.getCreatedAt(),
                order.getTotalPrice(),
                items
        );
    }
}
