package ru.mdemidkin.intershop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mdemidkin.intershop.repository.OrderRepository;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

}
