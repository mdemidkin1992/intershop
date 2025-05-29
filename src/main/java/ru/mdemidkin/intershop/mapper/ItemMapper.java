package ru.mdemidkin.intershop.mapper;

import org.springframework.stereotype.Component;
import ru.mdemidkin.intershop.model.Item;
import ru.mdemidkin.intershop.model.OrderItem;

import java.math.BigDecimal;
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

    public Item toItem(Item item, OrderItem orderItem) {
        item.setCount(orderItem.getQuantity());
        return item;
    }

}
