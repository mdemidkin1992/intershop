CREATE TABLE order_items
(
    id             BIGSERIAL PRIMARY KEY,
    order_id       BIGINT         NOT NULL REFERENCES orders (id),
    item_id        BIGINT         NOT NULL REFERENCES items (id),
    quantity       INT            NOT NULL,
    price_per_item DECIMAL(10, 2) NOT NULL,
    CONSTRAINT unique_order_item UNIQUE (order_id, item_id)
);
