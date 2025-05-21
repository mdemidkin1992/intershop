package ru.mdemidkin.intershop.dto;

import ru.mdemidkin.intershop.model.Item;
import ru.mdemidkin.intershop.model.enums.SortType;

import java.util.List;

public record ItemsSortedSearchPageDto(
        String search,
        SortType sortType,
        PagingDto responsePagingDto,
        List<List<Item>> itemsTile
) {
}
