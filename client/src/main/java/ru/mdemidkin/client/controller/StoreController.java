package ru.mdemidkin.client.controller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.mdemidkin.client.dto.CartItemListDto;
import ru.mdemidkin.client.model.enums.ItemAction;
import ru.mdemidkin.client.model.enums.SortType;
import ru.mdemidkin.client.service.ItemService;
import ru.mdemidkin.client.service.OrderService;
import ru.mdemidkin.client.service.PaymentService;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class StoreController {

    private final ItemService itemService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    @GetMapping("/")
    public Mono<String> redirectToMain() {
        return Mono.just("redirect:/main/items");
    }

    @GetMapping("/main/items")
    public Mono<String> getItems(@RequestParam(defaultValue = "") String search,
                                 @RequestParam(defaultValue = "NO") SortType sort,
                                 @RequestParam(defaultValue = "1") int pageNumber,
                                 @RequestParam(defaultValue = "10") int pageSize,
                                 Model model,
                                 Principal principal) {

        String username = principal != null
                ? principal.getName()
                : StringUtils.EMPTY;

        return itemService.searchItems(search, sort, pageNumber, pageSize, username)
                .map(result -> {
                    model.addAttribute("items", result.itemsTile());
                    model.addAttribute("search", result.search());
                    model.addAttribute("sort", result.sortType());
                    model.addAttribute("paging", result.responsePagingDto());
                    return "main";
                });
    }

    @PostMapping("/main/items/{id}")
    @PreAuthorize("isAuthenticated() and hasAuthority('USER')")
    public Mono<String> modifyItemInCart(@PathVariable Long id,
                                         ServerWebExchange exchange,
                                         Principal principal) {
        String username = principal.getName();
        return exchange.getFormData()
                .map(data -> data.getFirst("action"))
                .map(ItemAction::valueOf)
                .flatMap(action -> itemService.updateCartItem(id, action, username))
                .then(Mono.just("redirect:/main/items"));
    }

    @GetMapping("/cart/items")
    @PreAuthorize("isAuthenticated() and hasAuthority('USER')")
    public Mono<String> getCartItems(Model model, Principal principal) {
        String username = principal.getName();
        return itemService.getCartItemListDto(username)
                .map(dto -> {
                    model.addAttribute("items", dto.items());
                    model.addAttribute("total", dto.cartTotal());
                    model.addAttribute("empty", dto.isCartEmpty());
                    return "cart";
                });
    }

    @PostMapping("/cart/items/{id}")
    @PreAuthorize("isAuthenticated() and hasAuthority('USER')")
    public Mono<String> modifyCartItem(@PathVariable Long id,
                                       ServerWebExchange exchange,
                                       Principal principal) {
        String username = principal.getName();
        return exchange.getFormData()
                .map(data -> data.getFirst("action"))
                .map(ItemAction::valueOf)
                .flatMap(action -> itemService.updateCartItem(id, action, username))
                .then(Mono.just("redirect:/cart/items"));
    }

    @GetMapping("/items/{id}")
    public Mono<String> getItem(@PathVariable Long id,
                                Model model,
                                Principal principal) {
        String username = principal != null
                ? principal.getName()
                : StringUtils.EMPTY;

        return itemService.getById(id, username)
                .map(item -> {
                    model.addAttribute("item", item);
                    return "item";
                });
    }

    @PostMapping("/items/{id}")
    @PreAuthorize("isAuthenticated() and hasAuthority('USER')")
    public Mono<String> modifyItemFromCard(@PathVariable Long id,
                                           ServerWebExchange exchange,
                                           Principal principal) {
        String username = principal.getName();
        return exchange.getFormData()
                .map(data -> data.getFirst("action"))
                .map(ItemAction::valueOf)
                .flatMap(action -> itemService.updateCartItem(id, action, username))
                .then(Mono.just("redirect:/items/" + id));
    }

    @PostMapping("/buy")
    @PreAuthorize("isAuthenticated() and hasAuthority('USER')")
    public Mono<String> buyItems(Model model, Principal principal) {
        String username = principal.getName();
        return itemService.getCartItemListDto(username)
                .map(CartItemListDto::items)
                .flatMap(items -> {
                    double totalPrice = orderService.calculateTotalPrice(items);
                    return paymentService.processOrderPayment(totalPrice, username)
                            .flatMap(paymentSuccess -> {
                                if (paymentSuccess) {
                                    return orderService.createOrder(username)
                                            .map(order -> "redirect:/orders/" + order.getId() + "?newOrder=true");
                                } else {
                                    model.addAttribute("error", "Не достаточно средств");
                                    return Mono.just("error");
                                }
                            });
                });
    }

    @GetMapping("/orders")
    @PreAuthorize("isAuthenticated() and hasAuthority('USER')")
    public Mono<String> getOrders(Model model, Principal principal) {
        String userName = principal.getName();
        return orderService.findAll(userName)
                .collectList()
                .map(orders -> {
                    model.addAttribute("orders", orders);
                    return "orders";
                });
    }

    @GetMapping("/orders/{id}")
    @PreAuthorize("isAuthenticated() and hasAuthority('USER')")
    public Mono<String> getOrder(@PathVariable Long id,
                                 @RequestParam(defaultValue = "false") boolean newOrder,
                                 Model model) {
        return orderService.findById(id)
                .map(order -> {
                    model.addAttribute("order", order);
                    model.addAttribute("newOrder", newOrder);
                    return "order";
                });
    }
}
