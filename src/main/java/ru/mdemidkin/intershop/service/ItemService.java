package ru.mdemidkin.intershop.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mdemidkin.intershop.controller.dto.CartItemListDto;
import ru.mdemidkin.intershop.controller.dto.ItemAction;
import ru.mdemidkin.intershop.controller.dto.Paging;
import ru.mdemidkin.intershop.controller.dto.SortType;
import ru.mdemidkin.intershop.model.CartItem;
import ru.mdemidkin.intershop.model.Item;
import ru.mdemidkin.intershop.repository.ItemRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final CartService cartService;

    @Transactional(readOnly = true)
    public List<List<Item>> findAll(String search, SortType sortType, int pageNumber, int pageSize) {
        Sort sort = getSort(sortType);
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sort);

        Page<Item> itemPage;
        if (search == null || search.isBlank()) {
            itemPage = itemRepository.findAll(pageable);
        } else {
            itemPage = itemRepository.findByTitleOrDescriptionContaining(search, pageable);
        }

        Paging responsePaging = new Paging(
                pageNumber,
                pageSize,
                itemPage.hasNext(),
                itemPage.hasPrevious()
        );

        //todo
        // 1. проставить count
        // 2. преобразовать в List<List>

        return null;
    }

    @Transactional(readOnly = true)
    public Item getById(Long id) {
        Item item = itemRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        cartService.findItemById(item.getId()).ifPresent(cartItem -> item.setCount(cartItem.getQuantity()));
        return item;
    }

    @Transactional
    public void updateCartItem(Long itemId, ItemAction action) {
        Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);
        Optional<CartItem> optionalCartItem = cartService.findItemById(itemId);

        switch (action) {
            case PLUS:
                if (optionalCartItem.isPresent()) {
                    CartItem cartItem = optionalCartItem.get();
                    cartItem.setQuantity(cartItem.getQuantity() + 1);
                    cartService.saveOrUpdate(cartItem);
                } else {
                    CartItem cartItem = CartItem.builder()
                            .item(item)
                            .quantity(1)
                            .build();
                    cartService.saveOrUpdate(cartItem);
                }
                break;

            case MINUS:
                if (optionalCartItem.isPresent()) {
                    CartItem cartItem = optionalCartItem.get();
                    Integer quantity = cartItem.getQuantity();
                    if (quantity > 1) {
                        cartItem.setQuantity(quantity + 1);
                        cartService.saveOrUpdate(cartItem);
                    } else {
                        cartService.delete(cartItem);
                    }
                }
                break;

            case DELETE:
                optionalCartItem.ifPresent(cartService::delete);
                break;
        }
    }

    @Transactional(readOnly = true)
    public CartItemListDto getCartItemListDto() {
        List<Item> itemsFromCart = getItemsFromCart();
        return new CartItemListDto(
                itemsFromCart,
                getTotal(itemsFromCart),
                itemsFromCart.isEmpty()
        );
    }

    private Double getTotal(List<Item> items) {
        return items.stream()
                .map(i -> (i.getPrice() * i.getCount()))
                .reduce(0.0, Double::sum);
    }

    private List<Item> getItemsFromCart() {
        List<CartItem> cartItems = cartService.getAll();
        return cartItems.stream()
                .map(cartItem -> {
                    Item item = cartItem.getItem();
                    item.setCount(cartItem.getQuantity());
                    return item;
                }).toList();
    }

    private Sort getSort(SortType sortType) {
        Sort sort = Sort.unsorted();
        if (sortType == SortType.ALPHA) {
            sort = Sort.by("title").ascending();
        } else if (sortType == SortType.PRICE) {
            sort = Sort.by("price").ascending();
        }
        return sort;
    }

}
