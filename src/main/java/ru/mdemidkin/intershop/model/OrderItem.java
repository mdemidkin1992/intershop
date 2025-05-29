package ru.mdemidkin.intershop.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "order_items")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrderItem {

    @Id
    @EqualsAndHashCode.Include
    private Long id;

    @Column("order_id")
    private Long orderId;

    @Column("item_id")
    private Long itemId;

    @Column("quantity")
    private Integer quantity;

    @Column("price_per_item")
    private Double pricePerItem;
}
