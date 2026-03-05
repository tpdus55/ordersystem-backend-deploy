package com.example.ordersystem.ordering.service;

import com.example.ordersystem.common.service.SseAlarmService;
import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.member.repository.MemberRepository;
import com.example.ordersystem.ordering.domain.Ordering;
import com.example.ordersystem.ordering.domain.OrderDetail;
import com.example.ordersystem.ordering.dtos.OrderListDto;
import com.example.ordersystem.ordering.dtos.OrderCreateDto;
import com.example.ordersystem.ordering.repository.OrderDetailRepository;
import com.example.ordersystem.ordering.repository.OrderRepository;
import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final SseAlarmService sseAlarmService;
    private final RedisTemplate<String,String> redisTemplate;
    @Autowired
    public OrderService(OrderRepository orderRepository, OrderDetailRepository orderDetailRepository, MemberRepository memberRepository, ProductRepository productRepository, SseAlarmService sseAlarmService, @Qualifier("stockInventory") RedisTemplate<String, String> redisTemplate) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
        this.sseAlarmService = sseAlarmService;
        this.redisTemplate = redisTemplate;
    }
//    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Long save(List<OrderCreateDto> dtoList){
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()-> new EntityNotFoundException("없는 이메일 입니다."));

        Ordering order = Ordering.builder()
                .member(member)
                .build();
        orderRepository.save(order);
        for(OrderCreateDto dto : dtoList){
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(()-> new EntityNotFoundException("상품이 존재하지 않습니다."));
            if(product.getStockQuantity()< dto.getProductCount()){
                throw new IllegalArgumentException("재고가 부족합니다.");
            }
            product.updateStockQuantity(dto.getProductCount());
            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .quantity(dto.getProductCount())
                    .build();
            orderDetailRepository.save(detail);

        }
        return order.getId();
    }

    @Transactional(readOnly = true)
    public List<OrderListDto> findAll(){
        List<Ordering> orderList = orderRepository.findAll();
        List<OrderListDto> orderListDtos = new ArrayList<>();
        for(Ordering o : orderList){
            OrderListDto dto = OrderListDto.fromEntity(o);
            orderListDtos.add(dto);
        }
        return orderListDtos;
    }

    @Transactional(readOnly = true)
    public List<OrderListDto> myorders(){
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()-> new EntityNotFoundException("없는 이메일 입니다."));

        List<Ordering> orderList = orderRepository.findAllByMember(member);
        List<OrderListDto> orderListDtos = new ArrayList<>();
        for(Ordering o : orderList){
            OrderListDto dto = OrderListDto.fromEntity(o);
            orderListDtos.add(dto);
        }
        return orderListDtos;
    }

}



