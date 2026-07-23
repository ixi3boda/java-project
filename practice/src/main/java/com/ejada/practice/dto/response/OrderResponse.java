package com.ejada.practice.dto.response;

import com.ejada.practice.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private OrderStatus status;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private List<OrderItemResponse> items;
}
