package ru.mdemidkin.intershop.controller;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.mdemidkin.intershop.config.PostgresTestcontainersConfig;
import ru.mdemidkin.intershop.model.Order;
import ru.mdemidkin.intershop.model.enums.SortType;
import ru.mdemidkin.intershop.service.OrderService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class StoreControllerIntegrationTest extends PostgresTestcontainersConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderService orderService;

    @Test
    @SneakyThrows
    void redirectToMain_shouldRedirectToMainItems() {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main/items"));
    }

    @Test
    @SneakyThrows
    void getItems_shouldReturnHtmlWithItems() {
        mockMvc.perform(get("/main/items")
                        .param("search", "")
                        .param("sort", "NO")
                        .param("pageNumber", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("main.html"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("search"))
                .andExpect(model().attributeExists("sort"))
                .andExpect(model().attributeExists("paging"));
    }

    @Test
    @SneakyThrows
    void modifyItemInCart_shouldUpdateCartAndRedirect() {
        mockMvc.perform(post("/main/items/1")
                        .param("action", "plus"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main/items"));
    }

    @Test
    @SneakyThrows
    void getCartItems_shouldReturnHtmlWithCartItems() {
        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("cart.html"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attributeExists("empty"));
    }

    @Test
    @SneakyThrows
    void modifyCartItem_shouldUpdateCartAndRedirect() {
        mockMvc.perform(post("/cart/items/1")
                        .param("action", "plus"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart/items"));

        mockMvc.perform(post("/cart/items/1")
                        .param("action", "minus"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart/items"));

        mockMvc.perform(post("/cart/items/1")
                        .param("action", "delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart/items"));
    }

    @Test
    @SneakyThrows
    void getItem_shouldReturnHtmlWithItem() {
        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("item.html"))
                .andExpect(model().attributeExists("item"));
    }

    @Test
    @SneakyThrows
    void modifyItemFromCard_shouldUpdateCartAndRedirect() {
        mockMvc.perform(post("/items/1")
                        .param("action", "plus"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items/1"));
    }

    @Test
    @SneakyThrows
    void buyItems_shouldCreateOrderAndRedirect() {
        mockMvc.perform(post("/main/items/1")
                .param("action", "plus"));

        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/orders/*?newOrder=true"));
    }

    @Test
    @SneakyThrows
    void getOrders_shouldReturnHtmlWithOrders() {
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("orders.html"))
                .andExpect(model().attributeExists("orders"));
    }

    @Test
    @SneakyThrows
    void getOrder_shouldReturnHtmlWithOrder() {
        mockMvc.perform(post("/main/items/1")
                .param("action", "plus"));

        mockMvc.perform(post("/buy"));

        Order order = orderService.findAll().get(0);

        mockMvc.perform(get("/orders/" + order.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("order.html"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attributeExists("newOrder"));
    }

    @Test
    @SneakyThrows
    void getOrder_withNewOrderParam_shouldShowCongratulations() {
        mockMvc.perform(post("/main/items/1")
                .param("action", "plus"));

        mockMvc.perform(post("/buy"));

        Order order = orderService.findAll().get(0);

        mockMvc.perform(get("/orders/" + order.getId())
                        .param("newOrder", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("order.html"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attribute("newOrder", true));
    }

    @Test
    @SneakyThrows
    void getItems_withAlphaSort_shouldSortItemsAlphabetically() {
        mockMvc.perform(get("/main/items")
                        .param("sort", "ALPHA"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("sort", SortType.ALPHA));
    }

    @Test
    @SneakyThrows
    void getItems_withPriceSort_shouldSortItemsByPrice() {
        mockMvc.perform(get("/main/items")
                        .param("sort", "PRICE"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("sort", SortType.PRICE));
    }

    @Test
    @SneakyThrows
    void getItems_withCustomPageSize_shouldLimitNumberOfItems() {
        mockMvc.perform(get("/main/items")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("paging"));
    }

    @Test
    @SneakyThrows
    void getItems_withCustomPageNumber_shouldShowSpecificPage() {
        mockMvc.perform(get("/main/items")
                        .param("pageNumber", "2"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("paging"));
    }
}