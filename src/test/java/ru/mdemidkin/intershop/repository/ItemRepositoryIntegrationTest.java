package ru.mdemidkin.intershop.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.mdemidkin.intershop.config.PostgresTestcontainersConfig;
import ru.mdemidkin.intershop.model.Item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ItemRepositoryIntegrationTest extends PostgresTestcontainersConfig {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void findByTitleOrDescriptionContaining_withMatchingTitle_shouldReturnItems() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Item> result = itemRepository.findByTitleOrDescriptionContaining("Ноутбук", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Ноутбук Lenovo ThinkPad X1", result.getContent().get(0).getTitle());
    }

    @Test
    void findByTitleOrDescriptionContaining_withMatchingDescription_shouldReturnItems() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Item> result = itemRepository.findByTitleOrDescriptionContaining("шумоподавлением", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Наушники Sony WH-1000XM4", result.getContent().get(0).getTitle());
    }

    @Test
    void findByTitleOrDescriptionContaining_withPartialMatch_shouldReturnItems() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Item> result = itemRepository.findByTitleOrDescriptionContaining("монитор", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Монитор LG UltraFine 4K", result.getContent().get(0).getTitle());
    }

    @Test
    void findByTitleOrDescriptionContaining_withCaseInsensitiveMatch_shouldReturnItems() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Item> result = itemRepository.findByTitleOrDescriptionContaining("SAMSUNG", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Смартфон Samsung Galaxy S23", result.getContent().get(0).getTitle());
    }

    @Test
    void findByTitleOrDescriptionContaining_withCommonWord_shouldReturnMultipleItems() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Item> result = itemRepository.findByTitleOrDescriptionContaining("с", pageable);

        assertEquals(4, result.getTotalElements());
        assertTrue(result.getContent().stream()
                .anyMatch(item -> item.getTitle().equals("Наушники Sony WH-1000XM4")));
        assertTrue(result.getContent().stream()
                .anyMatch(item -> item.getTitle().equals("Монитор LG UltraFine 4K")));
    }

    @Test
    void findByTitleOrDescriptionContaining_withNoMatch_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Item> result = itemRepository.findByTitleOrDescriptionContaining("несуществующий", pageable);

        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void findByTitleOrDescriptionContaining_withSorting_shouldReturnSortedItems() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("price").descending());

        Page<Item> result = itemRepository.findByTitleOrDescriptionContaining("", pageable);

        assertEquals(4, result.getTotalElements());
        assertEquals("Ноутбук Lenovo ThinkPad X1", result.getContent().get(0).getTitle());
        assertEquals("Смартфон Samsung Galaxy S23", result.getContent().get(1).getTitle());
        assertEquals("Монитор LG UltraFine 4K", result.getContent().get(2).getTitle());
        assertEquals("Наушники Sony WH-1000XM4", result.getContent().get(3).getTitle());
    }

    @Test
    void findByTitleOrDescriptionContaining_withPagination_shouldReturnCorrectPage() {
        Pageable firstPage = PageRequest.of(0, 2, Sort.by("title").ascending());
        Pageable secondPage = PageRequest.of(1, 2, Sort.by("title").ascending());

        Page<Item> firstPageResult = itemRepository.findByTitleOrDescriptionContaining("", firstPage);
        Page<Item> secondPageResult = itemRepository.findByTitleOrDescriptionContaining("", secondPage);

        assertEquals(2, firstPageResult.getContent().size());
        assertEquals(2, secondPageResult.getContent().size());
        assertEquals(4, firstPageResult.getTotalElements());
        assertEquals(4, secondPageResult.getTotalElements());

        assertEquals("Монитор LG UltraFine 4K", firstPageResult.getContent().get(0).getTitle());
        assertEquals("Наушники Sony WH-1000XM4", firstPageResult.getContent().get(1).getTitle());

        assertEquals("Ноутбук Lenovo ThinkPad X1", secondPageResult.getContent().get(0).getTitle());
        assertEquals("Смартфон Samsung Galaxy S23", secondPageResult.getContent().get(1).getTitle());
    }
}