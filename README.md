# Intershop - Витрина интернет-магазина

Интернет-магазин, реализованный с использованием Spring Boot и Thymeleaf.
Позволяет просматривать товары, управлять корзиной и оформлять заказы.

## Архитектура проекта

Проект имеет многомодульную архитектуру:

* **client** - веб-интерфейс витрины (порт `8080`)
* **server** - сервис платежей и REST API (порт `8081`)

## Технологии

* Spring Boot WebFlux (реактивный стек)
* R2DBC (реактивная драйвер PostgreSQL)
* Redis (кеш)
* OpenAPI (автогенерация клиентов)
* TestContainers

## Запуск проекта

Для запуска с помощью Docker:

```bash
docker-compose up --build
```
Приложение станет доступно по адресу `http://localhost:8080`

## API функционал витрины (Client - порт 8080)

* GET     /                   → редирект на /main/items
* GET     /main/items         → главная страница с товарами
* POST    /main/items/{id}    → изменить количество товара (главная)
* GET     /items/{id}         → страница карточки товара
* POST    /items/{id}         → изменить количество товара (карточка)
* GET     /cart/items         → корзина
* POST    /cart/items/{id}    → изменить количество товара в корзине
* POST    /buy                → оформить заказ (очистить корзину)
* GET     /orders             → список всех заказов
* GET     /orders/{id}        → просмотр одного заказа

## REST API Сервиса платежей (Server - порт 8081)

* GET     /payments/health       → проверка состояния сервиса
* GET     /payments/balance/{id} → получить баланс пользователя
* POST    /payments/process      → обработать платеж

## Схема базы данных

Схема описана в UML файле `client/db.plantuml`

![Image](https://github.com/user-attachments/assets/a4eafe44-f122-454a-963b-124d1ecf78fc)

## Кеширование

Корзина пользователя кешируется в Redis для повышения производительности:

- `@Cacheable` - кеширование при получении товаров
- `@CachePut` - обновление кеша при изменении
- `@CacheEvict` - очистка кеша при удалении

## Генерация клиентов

OpenAPI спецификации находится в папках:
- `client/src/main/resources/api-spec.yaml`
- `server/src/main/resources/api-spec.yaml`

Автоматическая генерация Java-клиентов из OpenAPI спецификации:
- **Client** генерирует WebClient для вызова Server API
- **Server** генерирует Spring контроллеры и модели

## Тесты

Для запуска тестов выполнить команды:

```bash
gradle clean

# Все модули
gradle test

# Отдельные модули
gradle :client:test
gradle :server:test

```

Отобразятся ссылки для просмотра отчетов тестирования.