package ru.mdemidkin.intershop.webclient;

import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.mdemidkin.intershop.model.Item;

@RequiredArgsConstructor
public class IntershopWebClient {

    private final WebClient webClient;

    public Mono<Item> getItem(Long id) {
        return webClient.get()
                .uri("/items/" + id)
                .retrieve()
                .bodyToMono(Item.class);
    }



}
