package ru.mdemidkin.intershop.repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mdemidkin.intershop.model.Item;
import ru.mdemidkin.intershop.model.enums.SortType;

public interface ICustomItemRepository {

    Flux<Item> findItemsByOrderId(Long orderId);

    Mono<Long> getCountBySearch(String search);

    Flux<Item> getItemsBySearch(String search, SortType sortType, int pageNumber, int pageSize);
}
