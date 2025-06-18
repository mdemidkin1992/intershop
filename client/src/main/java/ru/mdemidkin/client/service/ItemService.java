package ru.mdemidkin.client.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mdemidkin.client.dto.CartItemListDto;
import ru.mdemidkin.client.dto.ItemsSortedSearchPageDto;
import ru.mdemidkin.client.dto.PagingDto;
import ru.mdemidkin.client.model.CartItem;
import ru.mdemidkin.client.model.Item;
import ru.mdemidkin.client.model.enums.ItemAction;
import ru.mdemidkin.client.model.enums.SortType;
import ru.mdemidkin.client.repository.ItemRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final CartService cartService;
    private final UserService userService;

    @Cacheable(cacheNames = "searchItems", key = "{#search, #sortType, #pageNumber, #pageSize, #username}")
    public Mono<ItemsSortedSearchPageDto> searchItems(String search, SortType sortType, int pageNumber, int pageSize, String username) {
        Mono<Long> totalCount = itemRepository.getCountBySearch(search);

        Flux<Item> itemFlux;
        if (StringUtils.isBlank(username)) {
            itemFlux = itemRepository.getItemsBySearch(
                            search, sortType, pageNumber, pageSize)
                    .flatMap(this::setItemQuantityToZero);
        } else {
            itemFlux = userService.findByUsername(username)
                    .flatMapMany(user -> itemRepository.getItemsBySearch(
                                    search, sortType, pageNumber, pageSize)
                            .flatMap(item -> setItemQuantity(item, user.getId())));
        }

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

    @Cacheable(cacheNames = "item", key = "{#id, #username}")
    public Mono<Item> getById(Long id, String username) {
        return itemRepository.findById(id)
                .flatMap(item -> {
                    if (StringUtils.isBlank(username)) {
                        return setItemQuantityToZero(item);
                    } else {
                        return userService.findByUsername(username)
                                .flatMap(user -> setItemQuantity(item, user.getId()));
                    }
                });
    }

    @CacheEvict(cacheNames = "cartItems", allEntries = true)
    public Mono<CartItem> updateCartItem(Long itemId, ItemAction action, String username) {
        return userService.findByUsername(username)
                .flatMap(user -> cartService.findCartItem(itemId, user.getId())
                        .switchIfEmpty(createNewCartItem(itemId, action, user.getId()))
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
                        })
                );
    }

    public Mono<List<Item>> getByOrderId(Long orderId) {
        return itemRepository.findItemsByOrderId(orderId).collectList();
    }

    private Mono<Item> setItemQuantityToZero(Item item) {
        item.setCount(0);
        return Mono.just(item);
    }

    private Mono<CartItem> createNewCartItem(Long itemId, ItemAction action, Long userId) {
        if (action.equals(ItemAction.plus)) {
            return itemRepository.findById(itemId)
                    .map(item -> {
                        CartItem cartItem = new CartItem();
                        cartItem.setItemId(itemId);
                        cartItem.setQuantity(0);
                        cartItem.setUserId(userId);
                        return cartItem;
                    });
        } else {
            return Mono.empty();
        }
    }

    @Cacheable(cacheNames = "cartItems")
    public Mono<CartItemListDto> getCartItemListDto(String username) {
        return getItemsFromCart(username).collectList()
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

    private Mono<Item> setItemQuantity(Item item, Long userId) {
        return cartService.findCartItem(item.getId(), userId)
                .doOnNext(cartItem -> item.setCount(cartItem.getQuantity()))
                .thenReturn(item);
    }

    private Double getTotal(List<Item> items) {
        return items.stream()
                .map(i -> (i.getPrice() * i.getCount()))
                .reduce(0.0, Double::sum);
    }

    private Flux<Item> getItemsFromCart(String username) {
        return cartService.getAll(username)
                .flatMap(cartItem ->
                        getById(cartItem.getItemId(), username)
                                .doOnNext(item -> item.setCount(cartItem.getQuantity()))
                );
    }
}
