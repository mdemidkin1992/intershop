package ru.mdemidkin.client.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.mdemidkin.client.config.TestContainersConfig;
import ru.mdemidkin.client.model.Item;
import ru.mdemidkin.client.model.enums.SortType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ItemRepositoryIntegrationTest extends TestContainersConfig {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    @DisplayName("Подсчет всех товаров должен вернуть четыре записи")
    void countAllItems_shouldReturnFour() {
        Long count = itemRepository.getCountBySearch("").block();

        assertThat(count)
                .withFailMessage("Ожидалось 4 товара, но получили %d", count)
                .isEqualTo(4L);
    }

    @Test
    @DisplayName("Поиск товаров по алфавиту")
    void findItemsBySearch_alphaSort_shouldReturnInAlphaOrder() {
        List<Item> items = itemRepository
                .getItemsBySearch("", SortType.ALPHA, 1, 10)
                .collectList()
                .block();

        assertThat(items)
                .withFailMessage("Список не должен быть пустым")
                .isNotEmpty();
        assertThat(items)
                .extracting(Item::getTitle)
                .withFailMessage("Первый элемент должен быть Monitor...")
                .first()
                .satisfies(title -> assertThat(title.toLowerCase()).contains("lg"));
    }

    @Test
    @DisplayName("Получение товаров по ID заказа")
    void findItemsByOrderId_shouldReturnCorrectItemsAndCounts() {
        List<Item> orderItems = itemRepository.findItemsByOrderId(1L)
                .collectList()
                .block();

        assertThat(orderItems)
                .withFailMessage("В заказе №1 должно быть 2 товара")
                .hasSize(2);
        assertThat(orderItems.get(0).getId()).isEqualTo(1L);
        assertThat(orderItems.get(0).getCount()).isEqualTo(1);
        assertThat(orderItems.get(1).getId()).isEqualTo(3L);
        assertThat(orderItems.get(1).getCount()).isEqualTo(2);
    }
}
