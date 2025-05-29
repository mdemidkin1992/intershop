package ru.mdemidkin.intershop.dto;

import ru.mdemidkin.intershop.model.Item;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDto(
        Long id,
        LocalDateTime createdAt,
        Double totalPrice,
        List<Item> items
) {
}
