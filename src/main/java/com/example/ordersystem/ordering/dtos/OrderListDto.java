package com.example.ordersystem.ordering.dtos;

import com.example.ordersystem.ordering.domain.Ordering;
import com.example.ordersystem.ordering.domain.OrderDetail;
import com.example.ordersystem.ordering.domain.OrderStatus;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
public class OrderListDto {
    private Long id;
    private String memberEmail;
    private OrderStatus orderStatus;
    private List<OrderDetailDto> orderDetails;

    public static OrderListDto fromEntity(Ordering order){
        List<OrderDetailDto> orderDetailDtos = new ArrayList<>();
        for(OrderDetail orderDetail : order.getDetailList()){
            orderDetailDtos.add(OrderDetailDto.fromEntity(orderDetail));
        }
        OrderListDto orderListDto = OrderListDto.builder()
                .id(order.getId())
                .memberEmail(order.getMember().getEmail())
                .orderStatus(order.getOrderStatus())
                .orderDetails(orderDetailDtos)
                .build();
        return orderListDto;
    }

}
