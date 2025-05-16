package ru.mdemidkin.intershop.dto;

import ru.mdemidkin.intershop.model.Item;

import java.util.List;

public record CartItemListDto(
        List<Item> items,
        Double cartTotal,
        boolean isCartEmpty
) {
}
