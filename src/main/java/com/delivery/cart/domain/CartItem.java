package com.delivery.cart.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "cart_items")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @Column(nullable = false)
    private UUID menuItemId;

    private String itemName;
    private int quantity;

    @Column(precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> selectedModifiers;
}
