CREATE TABLE items
(
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255)   NOT NULL,
    description TEXT,
    img_path    VARCHAR(255),
    price       DECIMAL(10, 2) NOT NULL,
    stock_count INT            NOT NULL DEFAULT 0
);