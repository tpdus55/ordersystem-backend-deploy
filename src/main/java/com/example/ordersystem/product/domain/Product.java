package com.example.ordersystem.product.domain;

import com.example.ordersystem.common.time.BaseTime;
import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.product.dtos.ProductUpdateDto;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
@Entity
public class Product extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT),nullable = false)
    private Member member;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private Long price;
    private String category;
    @Column(nullable = false)
    private Long stockQuantity;
    private String imagePath;

    public void updateImagePath(String imagePath){
        this.imagePath = imagePath;
    }

    public void updateStockQuantity(int orderQuantity){
        this.stockQuantity = this.stockQuantity-orderQuantity;
    }

    public void updateProduct(ProductUpdateDto dto){
        this.name = dto.getName();
        this.category = dto.getCategory();
        this.stockQuantity = dto.getStockQuantity();
        this.price = dto.getPrice();
    }
}
