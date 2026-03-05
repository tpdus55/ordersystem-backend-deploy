package com.example.ordersystem.product.dtos;

import com.example.ordersystem.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProductListDto {
    private Long id;
    private String name;
    private Long price;
    private String category;
    private Long stockQuantity;
    private String imagePath;

    public static ProductListDto fromEntity(Product product){
        return ProductListDto.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imagePath(product.getImagePath())
                .build();
    }
}
