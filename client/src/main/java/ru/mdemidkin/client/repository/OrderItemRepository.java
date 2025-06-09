package ru.mdemidkin.client.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import ru.mdemidkin.client.model.OrderItem;

@Repository
public interface OrderItemRepository extends R2dbcRepository<OrderItem, Long> {
}
