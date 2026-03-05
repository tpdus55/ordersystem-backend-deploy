package com.example.ordersystem.product.service;

import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.member.repository.MemberRepository;
import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.dtos.*;
import com.example.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ProductService {
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final S3Client s3Client;
    private final RedisTemplate<String,String> redisTemplate;
    @Value("${aws.s3.bucket1}")
    private String bucket;

    @Autowired
    public ProductService(ProductRepository productRepository, MemberRepository memberRepository, S3Client s3Client, @Qualifier("stockInventory") RedisTemplate<String, String> redisTemplate) {
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
        this.s3Client = s3Client;
        this.redisTemplate = redisTemplate;
    }


    public Product save(ProductCreateDto productCreateDto){
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()-> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        Product product = productCreateDto.toEntity(member);
        productRepository.save(product);

        if(productCreateDto.getProductImage() != null){
            String fileName = "product-"+product.getId()+"-"+productCreateDto.getProductImage().getOriginalFilename();
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(productCreateDto.getProductImage().getContentType()) //image/jpeg, video/mp4,....
                    .build();
            try {
                s3Client.putObject(request, RequestBody.fromBytes(productCreateDto.getProductImage().getBytes()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String imageUrl = s3Client.utilities().getUrl(a->a.bucket(bucket).key(fileName)).toExternalForm();

            product.updateImagePath(imageUrl);
        }
//        동시성 문제 해결을 위해 상품등록 시 redis에 재고세팅
        redisTemplate.opsForValue().set(String.valueOf(product.getId()),String.valueOf(product.getStockQuantity()));
        return product;

    }
    @Transactional(readOnly = true)
    public ProductDetailDto productDetail(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("없는 아이디 입니다."));
        ProductDetailDto dto = ProductDetailDto.fromEntity(product);
        return dto;
    }
    @Transactional(readOnly = true)
    public Page<ProductListDto> productList(Pageable pageable, ProductSearchDto searchDto){
        Specification<Product> specification = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
//                분기처리
                List<Predicate> predicateList = new ArrayList<>();

//                root : 엔티티의 컬럼명을 접근하기 위한 객체, criteriaBuilder : 쿼리를 생성하기 위한 객체
                if(searchDto.getProductName() !=null){
                    predicateList.add(criteriaBuilder.like(root.get("name"), "%"+ searchDto.getProductName()+"%"));
                }
                if(searchDto.getCategory() !=null){
                    predicateList.add(criteriaBuilder.equal(root.get("category"),searchDto.getCategory()));
                }

                Predicate[] predicateArr = new Predicate[predicateList.size()];
                for(int i=0;i<predicateArr.length;i++){
                    predicateArr[i] = predicateList.get(i);
                }
//                Predicate에는 검색조건들이 담길것이고, 이 Predicate list를 한줄의 predicate조립.
                Predicate predicate = criteriaBuilder.and(predicateArr);
                return predicate;
            }
        };
        Page<Product> postList = productRepository.findAll(specification, pageable);
        return postList.map(p->ProductListDto.fromEntity(p));
    }

    public void update(Long id, ProductUpdateDto dto){
        Product product = productRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("없는 아이디 입니다."));
        product.updateProduct(dto);
//        팀플 -> 이미지 수정 : 부분삭제 or 전체 수정
        if(dto.getProductImage() !=null){
//            이미지를 수정하는 경우 : 삭제 후 추가
//            기존이미지를 파일명으로 삭제
            if(product.getImagePath() != null){
                String imgUrl = product.getImagePath();
                String fileName = imgUrl.substring(imgUrl.lastIndexOf("/")+1); //슬래시를 중심으로 파일명 찾는것
                s3Client.deleteObject(a-> a.bucket(bucket).key(fileName));
            }

//          신규이미지 등록
            String newFileName = "product-"+product.getId()+"-"+dto.getProductImage().getOriginalFilename();
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(newFileName)
                    .contentType(dto.getProductImage().getContentType()) //image/jpeg, video/mp4,....
                    .build();

            try {
                s3Client.putObject(request, RequestBody.fromBytes(dto.getProductImage().getBytes()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String newImageUrl = s3Client.utilities().getUrl(a->a.bucket(bucket).key(newFileName)).toExternalForm();

            product.updateImagePath(newImageUrl);
        }else{
//            이미지를 삭제하는 경우
            if(product.getImagePath() != null){
                String imgUrl = product.getImagePath();
                String fileName = imgUrl.substring(imgUrl.lastIndexOf("/")+1); //슬래시를 중심으로 파일명 찾는것
                s3Client.deleteObject(a-> a.bucket(bucket).key(fileName));
            }
        }

    }
}
