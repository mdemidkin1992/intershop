package ru.mdemidkin.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.mdemidkin.intershop.service.CartService;
import ru.mdemidkin.intershop.service.ItemService;
import ru.mdemidkin.intershop.service.OrderService;

@Controller
@RequiredArgsConstructor
public class StoreController {

    private final ItemService itemService;
    private final CartService cartService;
    private final OrderService orderService;

    @GetMapping("/")
    public String redirectToMain() {
        return "redirect:/main/items";
    }

    @GetMapping("/main/items")
    public String getItems(@RequestParam(defaultValue = "") String search,
                           @RequestParam(defaultValue = "NO") String sort,
                           @RequestParam(defaultValue = "10") int pageSize,
                           @RequestParam(defaultValue = "1") int pageNumber,
                           Model model) {

        return "main.html";
    }

    @PostMapping("/main/items/{id}")
    public String modifyItemInCart(@PathVariable Long id,
                                   @RequestParam String action) {
        return "redirect:/main/items";
    }

    @GetMapping("/cart/items")
    public String getCartItems(Model model) {
        return "cart.html";
    }

    @PostMapping("/cart/items/{id}")
    public String modifyCartItem(@PathVariable Long id,
                                 @RequestParam String action) {
        return "redirect:/cart/items";
    }

    @GetMapping("/items/{id}")
    public String getItem(@PathVariable Long id, Model model) {
        return "item.html";
    }

    @PostMapping("/items/{id}")
    public String modifyItemFromCard(@PathVariable Long id,
                                     @RequestParam String action) {
        return "redirect:/items/" + id;
    }

    @PostMapping("/buy")
    public String buyItems() {
        return "redirect:/orders/" + 1L + "?newOrder=true";
    }

    @GetMapping("/orders")
    public String getOrders(Model model) {
        return "orders.html";
    }

    @GetMapping("/orders/{id}")
    public String getOrder(@PathVariable Long id,
                           @RequestParam(defaultValue = "false") boolean newOrder,
                           Model model) {
        return "order.html";
    }
}
