package com.example.ordersystem.ordering.domain;

import com.example.ordersystem.common.time.BaseTime;
import com.example.ordersystem.product.domain.Product;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
@Entity
public class OrderDetail extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id",foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT),nullable = false)
    private Ordering order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id",foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT),nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity; //주문 수량


}
