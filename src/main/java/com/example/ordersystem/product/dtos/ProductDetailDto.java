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
public class ProductDetailDto {
    private Long id;
    private String name;
    private Long price;
    private String category;
    private Long stockQuantity;
    private String imagePath;

    public static ProductDetailDto fromEntity(Product product){
        return ProductDetailDto.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imagePath(product.getImagePath())
                .build();
    }

}
