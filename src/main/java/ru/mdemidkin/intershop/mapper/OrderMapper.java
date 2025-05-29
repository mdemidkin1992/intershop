package ru.mdemidkin.intershop.mapper;

import org.springframework.stereotype.Component;
import ru.mdemidkin.intershop.dto.OrderDto;
import ru.mdemidkin.intershop.model.Item;
import ru.mdemidkin.intershop.model.Order;

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
