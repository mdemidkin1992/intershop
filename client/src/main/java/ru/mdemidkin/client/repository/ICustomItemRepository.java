package ru.mdemidkin.client.repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mdemidkin.client.model.Item;
import ru.mdemidkin.client.model.enums.SortType;

public interface ICustomItemRepository {

    Flux<Item> findItemsByOrderId(Long orderId);

    Mono<Long> getCountBySearch(String search);

    Flux<Item> getItemsBySearch(String search, SortType sortType, int pageNumber, int pageSize);
}
