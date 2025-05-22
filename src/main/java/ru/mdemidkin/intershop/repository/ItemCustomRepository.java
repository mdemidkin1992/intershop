package ru.mdemidkin.intershop.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mdemidkin.intershop.mapper.ItemMapper;
import ru.mdemidkin.intershop.model.Item;
import ru.mdemidkin.intershop.model.enums.SortType;

import static org.springframework.data.relational.core.query.Criteria.empty;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class ItemCustomRepository {

    private final DatabaseClient client;
    private final ItemMapper mapper;
    private final R2dbcEntityTemplate template;

    public Flux<Item> findItemsByOrderId(Long orderId) {
        String sql = """
            SELECT i.id, i.title, i.description, i.img_path, i.price, i.stock_count,
                   oi.quantity as count, oi.price_per_item
            FROM items i
            INNER JOIN order_items oi ON i.id = oi.item_id
            WHERE oi.order_id = :orderId
            ORDER BY i.id
            """;

        return client.sql(sql)
                .bind("orderId", orderId)
                .fetch()
                .all()
                .map(mapper::toItem);
    }

    public Mono<Long> getCountBySearch(String search) {
        Criteria criteria = searchCriteria(search);
        return template.count(query(criteria), Item.class);
    }

    public Flux<Item> getItemsBySearch(String search, SortType sortType, int pageNumber, int pageSize){
        Sort sort = getSort(sortType);
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sort);
        Criteria criteria = searchCriteria(search);
        return template.select(Item.class)
                .matching(query(criteria).with(pageable))
                .all();
    }

    private Criteria searchCriteria(String search){
        return (search == null || search.isBlank())
                ? empty()
                : where("title").like("%" + search.toLowerCase() + "%")
                .or(where("description").like("%" + search.toLowerCase() + "%"));
    }

    private Sort getSort(SortType sortType) {
        Sort sort = Sort.unsorted();
        if (sortType == SortType.ALPHA) {
            sort = Sort.by("title").ascending();
        } else if (sortType == SortType.PRICE) {
            sort = Sort.by("price").ascending();
        }
        return sort;
    }

}
