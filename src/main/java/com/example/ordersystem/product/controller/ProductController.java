package com.example.ordersystem.product.controller;

import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.dtos.*;
import com.example.ordersystem.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;
    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

//    상품등록
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> save(@ModelAttribute ProductCreateDto productCreateDto){
        Product product = productService.save(productCreateDto);
       return ResponseEntity.status(HttpStatus.CREATED).body(product.getId());
    }
//    상품상세조회
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> productDetail(@PathVariable Long id){
        ProductDetailDto dto = productService.productDetail(id);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }
//    상품목록조회
    @GetMapping("/list")
    public Page<ProductListDto> productList(@PageableDefault(size= 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                            @ModelAttribute ProductSearchDto searchDto){
        return productService.productList(pageable,searchDto);
    }

//    상품수정
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id , @ModelAttribute ProductUpdateDto dto){
        productService.update(id,dto);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
