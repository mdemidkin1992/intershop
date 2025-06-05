package ru.mdemidkin.server.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.mdemidkin.intershop.server.api.HealthApi;

@RestController
@RequestMapping
public class HealthApiController implements HealthApi {

    @Override
    public Mono<ResponseEntity<String>> healthCheck(ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok("Payment Service is running"));
    }

}
