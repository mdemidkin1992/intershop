package ru.mdemidkin.client.dto;

import ru.mdemidkin.client.model.Item;
import ru.mdemidkin.client.model.enums.SortType;

import java.util.List;

public record ItemsSortedSearchPageDto(
        String search,
        SortType sortType,
        PagingDto responsePagingDto,
        List<List<Item>> itemsTile
) {
}
