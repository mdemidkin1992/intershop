package ru.mdemidkin.client.dto;

import ru.mdemidkin.client.model.Item;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDto(
        Long id,
        LocalDateTime createdAt,
        Double totalPrice,
        List<Item> items
) {
}
