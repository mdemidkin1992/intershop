package ru.mdemidkin.intershop.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mdemidkin.intershop.mapper.ItemMapper;
import ru.mdemidkin.intershop.model.Item;
import ru.mdemidkin.intershop.model.OrderItem;
import ru.mdemidkin.intershop.model.enums.SortType;

import static org.springframework.data.relational.core.query.Criteria.empty;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ICustomItemRepository {

    private final ItemMapper mapper;
    private final R2dbcEntityTemplate template;

    @Override
    public Flux<Item> findItemsByOrderId(Long orderId) {
        return template.select(OrderItem.class)
                .matching(query(where("order_id").is(orderId)))
                .all()
                .flatMap(orderItem -> template.select(Item.class)
                        .matching(query(Criteria.where("id").is(orderItem.getItemId())))
                        .one()
                        .map(item -> mapper.toItem(item, orderItem))
                );
    }

    @Override
    public Mono<Long> getCountBySearch(String search) {
        Criteria criteria = searchCriteria(search);
        return template.count(query(criteria), Item.class);
    }

    @Override
    public Flux<Item> getItemsBySearch(String search, SortType sortType, int pageNumber, int pageSize) {
        Sort sort = getSort(sortType);
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sort);
        Criteria criteria = searchCriteria(search);
        return template.select(Item.class)
                .matching(query(criteria).with(pageable))
                .all();
    }

    private Criteria searchCriteria(String search) {
        return (search == null || search.isBlank())
                ? empty()
                : where("title").like("%" + search.toLowerCase() + "%")
                .or(where("description").like("%" + search.toLowerCase() + "%"));
    }

    private Sort getSort(SortType sortType) {
        if (sortType == SortType.ALPHA) {
            return Sort.by("title").ascending();
        } else if (sortType == SortType.PRICE) {
            return Sort.by("price").ascending();
        }
        return Sort.by("title").ascending();
    }

}
