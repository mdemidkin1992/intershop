package ru.mdemidkin.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.mdemidkin.intershop.model.Order;
import ru.mdemidkin.intershop.model.enums.ItemAction;
import ru.mdemidkin.intershop.model.enums.SortType;
import ru.mdemidkin.intershop.service.ItemService;
import ru.mdemidkin.intershop.service.OrderService;

@Controller
@RequiredArgsConstructor
public class StoreController {

    private final ItemService itemService;
    private final OrderService orderService;

    @GetMapping("/")
    public Mono<String> redirectToMain() {
        return Mono.just("redirect:/main/items");
    }

    @GetMapping("/main/items")
    public Mono<String> getItems(@RequestParam(defaultValue = "") String search,
                                 @RequestParam(defaultValue = "NO") SortType sort,
                                 @RequestParam(defaultValue = "1") int pageNumber,
                                 @RequestParam(defaultValue = "10") int pageSize,
                                 Model model) {
        return itemService.searchItems(search, sort, pageNumber, pageSize)
                .map(result -> {
                    model.addAttribute("items", result.itemsTile());
                    model.addAttribute("search", result.search());
                    model.addAttribute("sort", result.sortType());
                    model.addAttribute("paging", result.responsePagingDto());
                    return "main.html";
                });
    }

    @PostMapping("/main/items/{id}")
    public Mono<String> modifyItemInCart(@PathVariable Long id,
                                         ServerWebExchange exchange) {
        return exchange.getFormData()
                .map(data -> data.getFirst("action"))
                .map(ItemAction::valueOf)
                .flatMap(action -> itemService.updateCartItem(id, action))
                .then(Mono.just("redirect:/main/items"));
    }

    @GetMapping("/cart/items")
    public Mono<String> getCartItems(Model model) {
        return itemService.getCartItemListDto()
                .map(dto -> {
                    model.addAttribute("items", dto.items());
                    model.addAttribute("total", dto.cartTotal());
                    model.addAttribute("empty", dto.isCartEmpty());
                    return "cart.html";
                });
    }

    @PostMapping("/cart/items/{id}")
    public Mono<String> modifyCartItem(@PathVariable Long id,
                                       ServerWebExchange exchange) {
        return exchange.getFormData()
                .map(data -> data.getFirst("action"))
                .map(ItemAction::valueOf)
                .flatMap(action -> itemService.updateCartItem(id, action))
                .then(Mono.just("redirect:/cart/items"));
    }

    @GetMapping("/items/{id}")
    public Mono<String> getItem(@PathVariable Long id, Model model) {
        return itemService.getById(id)
                .map(item -> {
                    model.addAttribute("item", item);
                    return "item.html";
                });
    }

    @PostMapping("/items/{id}")
    public Mono<String> modifyItemFromCard(@PathVariable Long id,
                                           ServerWebExchange exchange) {
        return exchange.getFormData()
                .map(data -> data.getFirst("action"))
                .map(ItemAction::valueOf)
                .flatMap(action -> itemService.updateCartItem(id, action))
                .then(Mono.just("redirect:/items/" + id));
    }

    @PostMapping("/buy")
    public Mono<String> buyItems() {
        return orderService.createOrder()
                .map(order -> "redirect:/orders/" + order.getId() + "?newOrder=true");
    }

    @GetMapping("/orders")
    public Mono<String> getOrders(Model model) {
        return orderService.findAll()
                .collectList()
                .map(orders -> {
                    model.addAttribute("orders", orders);
                    return "orders.html";
                });
    }

    @GetMapping("/orders/{id}")
    public Mono<String> getOrder(@PathVariable Long id,
                                 @RequestParam(defaultValue = "false") boolean newOrder,
                                 Model model) {
        return orderService.findById(id)
                .map(order -> {
                    model.addAttribute("order", order);
                    model.addAttribute("newOrder", newOrder);
                    return "order.html";
                });
    }
}
