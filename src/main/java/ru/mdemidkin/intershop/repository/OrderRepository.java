package ru.mdemidkin.intershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mdemidkin.intershop.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}
