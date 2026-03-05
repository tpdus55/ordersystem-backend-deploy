package com.example.ordersystem.ordering.controller;

import com.example.ordersystem.ordering.dtos.OrderCreateDto;
import com.example.ordersystem.ordering.dtos.OrderListDto;

import com.example.ordersystem.ordering.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordering")
public class OrderController {
    private final OrderService orderService;
    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

//    주문하기
    @PostMapping("/create")
    public ResponseEntity<?> save(@RequestBody List<OrderCreateDto> dtoList){
        Long id = orderService.save(dtoList);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

//   주문목록조회
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> findAll(){
        List<OrderListDto> orderListDtos = orderService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(orderListDtos);
    }

//   내 주문 목록조회
    @GetMapping("/myorders")
    public ResponseEntity<?> myorders(){
        List<OrderListDto> orderListDtos = orderService.myorders();
        return ResponseEntity.status(HttpStatus.OK).body(orderListDtos);
    }

}
