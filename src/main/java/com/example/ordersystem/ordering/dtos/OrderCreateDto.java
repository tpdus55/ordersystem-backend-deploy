package com.example.ordersystem.ordering.dtos;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderCreateDto {
    private Long productId;
    private int productCount;

}
