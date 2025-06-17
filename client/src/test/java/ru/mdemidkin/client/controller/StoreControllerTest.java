package ru.mdemidkin.client.controller;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mdemidkin.client.dto.CartItemListDto;
import ru.mdemidkin.client.dto.ItemsSortedSearchPageDto;
import ru.mdemidkin.client.dto.OrderDto;
import ru.mdemidkin.client.dto.PagingDto;
import ru.mdemidkin.client.model.Item;
import ru.mdemidkin.client.model.Order;
import ru.mdemidkin.client.model.enums.ItemAction;
import ru.mdemidkin.client.model.enums.SortType;
import ru.mdemidkin.client.service.ItemService;
import ru.mdemidkin.client.service.OrderService;
import ru.mdemidkin.client.service.PaymentService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class StoreControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ItemService itemService;

    @MockBean
    private OrderService orderService;

    @MockBean
    private PaymentService paymentService;

    @Test
    void shouldRedirectToMainItems() {
        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/main/items");
    }

    @Test
    void shouldGetItemsWithAnonymousUser() {
        Item item = createTestItem(1L, "Test Item", 10.0);
        PagingDto pagingDto = new PagingDto(1, 1, true, false);
        ItemsSortedSearchPageDto searchResult = new ItemsSortedSearchPageDto(
                "test",
                SortType.NO,
                pagingDto,
                List.of(List.of(item)));

        when(itemService.searchItems(anyString(), any(SortType.class), anyInt(), anyInt(), eq(StringUtils.EMPTY)))
                .thenReturn(Mono.just(searchResult));

        webTestClient.get()
                .uri("/main/items?search=test&sort=NO&pageNumber=1&pageSize=10")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class);
    }

    @Test
    @WithMockUser(username = "testuser", authorities = "USER")
    void shouldGetItemsWithAuthorizedUser() {
        Item item = createTestItem(1L, "Test Item", 10.0);
        PagingDto pagingDto = new PagingDto(1, 1, true, false);
        ItemsSortedSearchPageDto searchResult = new ItemsSortedSearchPageDto(
                "test",
                SortType.NO,
                pagingDto,
                List.of(List.of(item)));

        when(itemService.searchItems(anyString(), any(SortType.class), anyInt(), anyInt(), eq("testuser")))
                .thenReturn(Mono.just(searchResult));

        webTestClient.get()
                .uri("/main/items?search=test&sort=NO&pageNumber=1&pageSize=10")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class);
    }

    @Test
    void shouldGetItemsWithDefaultParameters() {
        Item item = createTestItem(1L, "Test Item", 10.0);
        PagingDto pagingDto = new PagingDto(1, 1, true, false);
        ItemsSortedSearchPageDto searchResult = new ItemsSortedSearchPageDto(
                "",
                SortType.NO,
                pagingDto,
                List.of(List.of(item)));

        when(itemService.searchItems("", SortType.NO, 1, 10, StringUtils.EMPTY))
                .thenReturn(Mono.just(searchResult));

        webTestClient.get()
                .uri("/main/items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockUser(username = "testuser", authorities = "USER")
    void shouldModifyItemInCart() {
        when(itemService.updateCartItem(eq(1L), eq(ItemAction.plus), eq("testuser")))
                .thenReturn(Mono.empty());

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("action", "plus");

        webTestClient.post()
                .uri("/main/items/1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/main/items");
    }

    @Test
    @WithMockUser(username = "testuser", authorities = "USER")
    void shouldGetCartItems() {
        Item item = createTestItem(1L, "Test Item", 10.0);
        CartItemListDto cartDto = new CartItemListDto(
                List.of(item),
                10.00,
                false);

        when(itemService.getCartItemListDto("testuser"))
                .thenReturn(Mono.just(cartDto));

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockUser(username = "testuser", authorities = "USER")
    void shouldModifyCartItem() {
        when(itemService.updateCartItem(eq(1L), eq(ItemAction.minus), eq("testuser")))
                .thenReturn(Mono.empty());

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("action", "minus");

        webTestClient.post()
                .uri("/cart/items/1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/cart/items");
    }

    @Test
    void shouldGetItemWithAnonymousUser() {
        Item item = createTestItem(1L, "Test Item", 10.0);
        when(itemService.getById(1L, StringUtils.EMPTY))
                .thenReturn(Mono.just(item));

        webTestClient.get()
                .uri("/items/1")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockUser(username = "testuser", authorities = "USER")
    void shouldGetItemWithRegisteredUser() {
        Item item = createTestItem(1L, "Test Item", 10.0);
        when(itemService.getById(1L, "testuser"))
                .thenReturn(Mono.just(item));

        webTestClient.get()
                .uri("/items/1")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockUser(username = "testuser", authorities = "USER")
    void shouldModifyItemFromCard() {
        when(itemService.updateCartItem(eq(1L), eq(ItemAction.plus), eq("testuser")))
                .thenReturn(Mono.empty());

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("action", "plus");

        webTestClient.post()
                .uri("/items/1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/items/1");
    }

    @Test
    @WithMockUser(username = "testuser", authorities = "USER")
    void shouldBuyItems() {
        Item item = createTestItem(1L, "Test Item", 10.0);
        CartItemListDto cartDto = new CartItemListDto(
                List.of(item),
                10.00,
                false);
        Order order = createTestOrder(1L);

        when(itemService.getCartItemListDto("testuser"))
                .thenReturn(Mono.just(cartDto));
        when(paymentService.processOrderPayment(any(), eq("testuser")))
                .thenReturn(Mono.just(true));
        when(orderService.createOrder("testuser"))
                .thenReturn(Mono.just(order));

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/orders/1?newOrder=true");
    }

    @Test
    @WithMockUser(username = "testuser", authorities = "USER")
    void shouldGetOrders() {
        Order order1 = createTestOrder(1L);
        Order order2 = createTestOrder(2L);

        List<Item> testItems1 = List.of(createTestItem(1L, "Test item 1", 10.0));
        List<Item> testItems2 = List.of(createTestItem(2L, "Test item 2", 15.0));

        OrderDto orderDto1 = new OrderDto(order1.getId(), order1.getCreatedAt(), order1.getTotalPrice(), testItems1);
        OrderDto orderDto2 = new OrderDto(order2.getId(), order2.getCreatedAt(), order2.getTotalPrice(), testItems2);

        when(orderService.findAll("testuser")).thenReturn(Flux.just(orderDto1, orderDto2));

        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockUser(username = "testuser", authorities = "USER")
    void shouldGetOrder() {
        Order order = createTestOrder(1L);
        List<Item> testItems = List.of(createTestItem(1L, "Test item 1", 10.0));
        OrderDto orderDto = new OrderDto(order.getId(), order.getCreatedAt(), order.getTotalPrice(), testItems);

        when(orderService.findById(1L)).thenReturn(Mono.just(orderDto));

        webTestClient.get()
                .uri("/orders/1")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockUser(username = "testuser", authorities = "USER")
    void shouldGetOrderWithNewOrderFlag() {
        Order order = createTestOrder(1L);
        List<Item> testItems = List.of(createTestItem(1L, "Test item 1", 10.0));
        OrderDto orderDto = new OrderDto(order.getId(), order.getCreatedAt(), order.getTotalPrice(), testItems);

        when(orderService.findById(1L)).thenReturn(Mono.just(orderDto));

        webTestClient.get()
                .uri("/orders/1?newOrder=true")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockUser(username = "testuser", authorities = "USER")
    void shouldHandleInvalidItemAction() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("action", "INVALID_ACTION");

        webTestClient.post()
                .uri("/main/items/1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    private Item createTestItem(Long id, String name, Double price) {
        Item item = new Item();
        item.setId(id);
        item.setTitle(name);
        item.setPrice(price);
        item.setDescription("Test description");
        item.setImgPath("test-image.jpg");
        return item;
    }

    private Order createTestOrder(Long id) {
        Order order = new Order();
        order.setId(id);
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalPrice(100.00);
        return order;
    }
}