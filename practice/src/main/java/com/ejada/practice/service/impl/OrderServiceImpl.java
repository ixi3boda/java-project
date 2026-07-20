package com.ejada.practice.service.impl;

import com.ejada.practice.dto.request.OrderItemRequest;
import com.ejada.practice.dto.request.OrderRequest;
import com.ejada.practice.dto.request.OrderStatusUpdateRequest;
import com.ejada.practice.dto.response.OrderItemResponse;
import com.ejada.practice.dto.response.OrderResponse;
import com.ejada.practice.entity.*;
import com.ejada.practice.exception.InsufficientStockException;
import com.ejada.practice.exception.ResourceNotFoundException;
import com.ejada.practice.repository.OrderRepository;
import com.ejada.practice.repository.ProductRepository;
import com.ejada.practice.repository.UserRepository;
import com.ejada.practice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OrderResponse placeOrder(String username, OrderRequest request) {

    }

    @Override
    public Page<OrderResponse> getMyOrders(String username, Pageable pageable) {

    }

    @Override
    public Page<OrderResponse> getAllOrders(OrderStatus status, Long userId, Pageable pageable) {

    }

    @Override
    public OrderResponse getOrderById(Long id, String username, boolean isAdmin) {

    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatusUpdateRequest request) {

    }

    private OrderResponse toResponse(Order order) {

    }
}
