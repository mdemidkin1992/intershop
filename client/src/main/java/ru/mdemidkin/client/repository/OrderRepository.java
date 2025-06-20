package ru.mdemidkin.client.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import ru.mdemidkin.client.model.Order;

@Repository
public interface OrderRepository extends R2dbcRepository<Order, Long> {
}
