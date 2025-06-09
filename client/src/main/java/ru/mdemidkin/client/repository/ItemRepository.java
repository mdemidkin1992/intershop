package ru.mdemidkin.client.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import ru.mdemidkin.client.model.Item;

@Repository
public interface ItemRepository extends
        R2dbcRepository<Item, Long>,
        ICustomItemRepository {
}
