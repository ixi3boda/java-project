package com.ejada.practice.service;

import com.ejada.practice.dto.request.OrderRequest;
import com.ejada.practice.dto.request.OrderStatusUpdateRequest;
import com.ejada.practice.dto.response.OrderResponse;
import com.ejada.practice.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponse placeOrder(String username, OrderRequest request);
    Page<OrderResponse> getMyOrders(String username, Pageable pageable);
    Page<OrderResponse> getAllOrders(OrderStatus status, Long userId, Pageable pageable);
    OrderResponse getOrderById(Long id, String username, boolean isAdmin);
    OrderResponse updateStatus(Long id, OrderStatusUpdateRequest request);
}
