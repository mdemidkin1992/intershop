package ru.mdemidkin.intershop.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import ru.mdemidkin.intershop.model.Item;

@Repository
public interface ItemRepository extends
        R2dbcRepository<Item, Long>,
        ICustomItemRepository {
}
