package ru.mdemidkin.intershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mdemidkin.intershop.model.CartItem;

@Repository
public interface CartRepository extends JpaRepository<CartItem, Long> {
}
