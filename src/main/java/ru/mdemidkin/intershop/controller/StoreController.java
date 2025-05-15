package ru.mdemidkin.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.mdemidkin.intershop.controller.dto.CartItemListDto;
import ru.mdemidkin.intershop.controller.dto.ItemAction;
import ru.mdemidkin.intershop.controller.dto.ItemsSortedSearchPageDto;
import ru.mdemidkin.intershop.controller.dto.SortType;
import ru.mdemidkin.intershop.model.Item;
import ru.mdemidkin.intershop.model.Order;
import ru.mdemidkin.intershop.service.ItemService;
import ru.mdemidkin.intershop.service.OrderService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StoreController {

    private final ItemService itemService;
    private final OrderService orderService;

    @GetMapping("/")
    public String redirectToMain() {
        return "redirect:/main/items";
    }

    @GetMapping("/main/items")
    public String getItems(@RequestParam(defaultValue = "") String search,
                           @RequestParam(defaultValue = "NO") SortType sort,
                           @RequestParam(defaultValue = "1") int pageNumber,
                           @RequestParam(defaultValue = "10") int pageSize,
                           Model model) {
        ItemsSortedSearchPageDto result = itemService.searchItems(search, sort, pageNumber, pageSize);
        model.addAttribute("items", result.itemsTile());
        model.addAttribute("search", result.search());
        model.addAttribute("sort", result.sortType());
        model.addAttribute("paging", result.responsePaging());
        return "main.html";
    }

    @PostMapping("/main/items/{id}")
    public String modifyItemInCart(@PathVariable Long id,
                                   @RequestParam ItemAction action) {
        itemService.updateCartItem(id, action);
        return "redirect:/main/items";
    }

    @GetMapping("/cart/items")
    public String getCartItems(Model model) {
        CartItemListDto dto = itemService.getCartItemListDto();
        model.addAttribute("items", dto.items());
        model.addAttribute("total", dto.cartTotal());
        model.addAttribute("empty", dto.isCartEmpty());
        return "cart.html";
    }

    @PostMapping("/cart/items/{id}")
    public String modifyCartItem(@PathVariable Long id,
                                 @RequestParam ItemAction action) {
        itemService.updateCartItem(id, action);
        return "redirect:/cart/items";
    }

    @GetMapping("/items/{id}")
    public String getItem(@PathVariable Long id, Model model) {
        Item item = itemService.getById(id);
        model.addAttribute("item", item);
        return "item.html";
    }

    @PostMapping("/items/{id}")
    public String modifyItemFromCard(@PathVariable Long id,
                                     @RequestParam ItemAction action) {
        itemService.updateCartItem(id, action);
        return "redirect:/items/" + id;
    }

    @PostMapping("/buy")
    public String buyItems() {
        Order order = orderService.createOrder();
        return "redirect:/orders/" + order.getId() + "?newOrder=true";
    }

    @GetMapping("/orders")
    public String getOrders(Model model) {
        List<Order> orders = orderService.findAll();
        model.addAttribute("orders", orders);
        return "orders.html";
    }

    @GetMapping("/orders/{id}")
    public String getOrder(@PathVariable Long id,
                           @RequestParam(defaultValue = "false") boolean newOrder,
                           Model model) {
        Order order = orderService.findById(id);
        model.addAttribute("order", order);
        model.addAttribute("newOrder", newOrder);
        return "order.html";
    }
}
