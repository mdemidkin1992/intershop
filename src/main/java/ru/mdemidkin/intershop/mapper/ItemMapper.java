package ru.mdemidkin.intershop.mapper;

import org.springframework.stereotype.Component;
import ru.mdemidkin.intershop.dto.ItemsSortedSearchPageDto;
import ru.mdemidkin.intershop.dto.PagingDto;
import ru.mdemidkin.intershop.model.Item;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class ItemMapper {

    public Item toItem(Map<String, Object> row) {
        return Item.builder()
                .id((Long) row.get("id"))
                .title((String) row.get("title"))
                .description((String) row.get("description"))
                .imgPath((String) row.get("img_path"))
                .price(((BigDecimal) row.get("price")).doubleValue())
                .stockCount((Integer) row.get("stock_count"))
                .count((Integer) row.get("count"))
                .build();
    }

//    public ItemsSortedSearchPageDto toDto(List<Item> items, long totalCount) {
//        boolean hasNext = (long) pageNumber * pageSize < total;
//        boolean hasPrevious = pageNumber > 1;
//
//        PagingDto pagingDto = new PagingDto(pageNumber, pageSize, hasNext, hasPrevious);
//        List<List<Item>> tiles = getItemsTile(items);
//
//        return new ItemsSortedSearchPageDto(search, sortType, pagingDto, tiles);
//
//    }


}
