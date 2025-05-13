CREATE TABLE cart_items
(
    id       BIGSERIAL PRIMARY KEY,
    item_id  BIGINT NOT NULL REFERENCES items (id),
    quantity INT    NOT NULL,
    CONSTRAINT unique_cart_item UNIQUE (item_id)
);