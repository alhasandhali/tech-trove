package com.spring.boot.ecommerce.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order-id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product-id")
    private Product product;

    private int quantity;

    private double price;
}
