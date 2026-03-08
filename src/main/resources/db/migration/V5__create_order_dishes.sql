CREATE TABLE order_dishes
(
    dish_id  BIGINT NOT NULL,
    order_id BIGINT NOT NULL,

    CONSTRAINT fk_orddis_dish
        FOREIGN KEY (dish_id) REFERENCES dishes(id),

    CONSTRAINT fk_orddis_order
        FOREIGN KEY (order_id) REFERENCES orders(id)
);