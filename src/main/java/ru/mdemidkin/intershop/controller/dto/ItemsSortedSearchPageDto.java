package ru.mdemidkin.intershop.controller.dto;

import ru.mdemidkin.intershop.model.Item;

import java.util.List;

public record ItemsSortedSearchPageDto(
        String search,
        SortType sortType,
        Paging responsePaging,
        List<List<Item>> itemsTile
) {
}
