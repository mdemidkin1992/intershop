package ru.mdemidkin.client.dto;

import ru.mdemidkin.client.model.Item;

import java.util.List;

public record CartItemListDto(
        List<Item> items,
        Double cartTotal,
        boolean isCartEmpty
) {
}
