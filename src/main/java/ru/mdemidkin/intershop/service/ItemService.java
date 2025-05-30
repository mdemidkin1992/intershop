package ru.mdemidkin.intershop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mdemidkin.intershop.dto.CartItemListDto;
import ru.mdemidkin.intershop.dto.ItemsSortedSearchPageDto;
import ru.mdemidkin.intershop.dto.PagingDto;
import ru.mdemidkin.intershop.model.CartItem;
import ru.mdemidkin.intershop.model.Item;
import ru.mdemidkin.intershop.model.enums.ItemAction;
import ru.mdemidkin.intershop.model.enums.SortType;
import ru.mdemidkin.intershop.repository.ItemRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final CartService cartService;

    @Cacheable(cacheNames = "searchItems", key = "{#search, #sortType, #pageNumber, #pageSize}")
    public Mono<ItemsSortedSearchPageDto> searchItems(String search, SortType sortType, int pageNumber, int pageSize) {
        Mono<Long> totalCount = itemRepository.getCountBySearch(search);
        Flux<Item> itemFlux = itemRepository.getItemsBySearch(search, sortType, pageNumber, pageSize)
                .flatMap(this::setItemQuantity);

        return itemFlux.collectList()
                .zipWith(totalCount)
                .map(tuple -> {
                    List<Item> items = tuple.getT1();
                    long total = tuple.getT2();

                    boolean hasNext = (long) pageNumber * pageSize < total;
                    boolean hasPrevious = pageNumber > 1;

                    PagingDto pagingDto = new PagingDto(pageNumber, pageSize, hasNext, hasPrevious);
                    List<List<Item>> tiles = getItemsTile(items);

                    return new ItemsSortedSearchPageDto(search, sortType, pagingDto, tiles);
                });
    }

    @Cacheable(cacheNames = "item", key = "#id")
    public Mono<Item> getById(Long id) {
        return itemRepository.findById(id)
                .flatMap(this::setItemQuantity);
    }

    @CacheEvict(cacheNames = "cartItems", allEntries = true)
    public Mono<CartItem> updateCartItem(Long itemId, ItemAction action) {
        return cartService.findItemById(itemId)
                .switchIfEmpty(createNewCartItem(itemId, action))
                .flatMap(cartItem -> {
                    switch (action) {
                        case plus:
                            cartItem.setQuantity(cartItem.getQuantity() + 1);
                            return cartService.saveOrUpdate(cartItem);

                        case minus:
                            int quantity = cartItem.getQuantity();
                            if (quantity > 1) {
                                cartItem.setQuantity(quantity - 1);
                                return cartService.saveOrUpdate(cartItem);
                            } else {
                                return cartService.delete(cartItem).then(Mono.empty());
                            }

                        case delete:
                            return cartService.delete(cartItem).then(Mono.empty());

                        default:
                            return Mono.empty();
                    }
                });
    }

    public Mono<List<Item>> getByOrderId(Long orderId) {
        return itemRepository.findItemsByOrderId(orderId).collectList();
    }

    private Mono<CartItem> createNewCartItem(Long itemId, ItemAction action) {
        if (action.equals(ItemAction.plus)) {
            return itemRepository.findById(itemId)
                    .map(item -> {
                        CartItem cartItem = new CartItem();
                        cartItem.setItemId(itemId);
                        cartItem.setQuantity(0);
                        return cartItem;
                    });
        } else {
            return Mono.empty();
        }
    }

    @Cacheable(cacheNames = "cartItems")
    public Mono<CartItemListDto> getCartItemListDto() {
        return getItemsFromCart().collectList()
                .map(list -> new CartItemListDto(list, getTotal(list), list.isEmpty()));
    }

    /**
     * Группируем товары по три в ряд (для представления плиткой)
     *
     * @param items список товаров на странице
     * @return плитку 3х3 товаров на странице
     */
    private List<List<Item>> getItemsTile(List<Item> items) {
        List<List<Item>> rows = new ArrayList<>();
        List<Item> currentRow = null;

        for (int i = 0; i < items.size(); i++) {
            if (i % 3 == 0) {
                currentRow = new ArrayList<>();
                rows.add(currentRow);
            }
            currentRow.add(items.get(i));
        }
        return rows;
    }

    private Mono<Item> setItemQuantity(Item item) {
        return cartService.findItemById(item.getId())
                .doOnNext(cartItem -> item.setCount(cartItem.getQuantity()))
                .thenReturn(item);
    }

    private Double getTotal(List<Item> items) {
        return items.stream()
                .map(i -> (i.getPrice() * i.getCount()))
                .reduce(0.0, Double::sum);
    }

    private Flux<Item> getItemsFromCart() {
        return cartService.getAll()
                .flatMap(cartItem ->
                        getById(cartItem.getItemId())
                                .doOnNext(item -> item.setCount(cartItem.getQuantity()))
                );
    }
}
