package ru.mdemidkin.intershop.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import ru.mdemidkin.intershop.model.Order;

@Repository
public interface OrderRepository extends R2dbcRepository<Order, Long> {
}
