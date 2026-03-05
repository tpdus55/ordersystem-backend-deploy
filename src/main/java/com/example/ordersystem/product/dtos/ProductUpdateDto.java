package com.example.ordersystem.product.dtos;

import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProductUpdateDto {
    private String name;
    private Long price;
    private String category;
    private Long stockQuantity;
//    이미지 수정은 일반적으로 별도의 api로 처리
    private MultipartFile productImage;

}
