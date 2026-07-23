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
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        Order order = Order.builder().user(user).orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING).totalAmount(BigDecimal.ZERO).build();
        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemRequest.getProductId()));
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }
            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
            productRepository.save(product);
            OrderItem item = OrderItem.builder().order(order).product(product).quantity(itemRequest.getQuantity())
                    .priceAtPurchase(product.getPrice()).build();
            items.add(item);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }
        order.setItems(items);
        order.setTotalAmount(total);
        return toResponse(orderRepository.save(order));
    }

    @Override
    public Page<OrderResponse> getMyOrders(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return orderRepository.findByUserId(user.getId(), pageable).map(this::toResponse);
    }

    @Override
    public Page<OrderResponse> getAllOrders(OrderStatus status, Long userId, Pageable pageable) {
        if (status != null && userId != null) return orderRepository.findByUserIdAndStatus(userId, status, pageable).map(this::toResponse);
        if (status != null) return orderRepository.findByStatus(status, pageable).map(this::toResponse);
        if (userId != null) return orderRepository.findByUserId(userId, pageable).map(this::toResponse);
        return orderRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public OrderResponse getOrderById(Long id, String username, boolean isAdmin) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        if (!isAdmin && !order.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You are not allowed to access this order");
        }
        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        order.setStatus(request.getStatus());
        return toResponse(orderRepository.save(order));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream().map(item -> OrderItemResponse.builder()
                .productId(item.getProduct().getId()).productName(item.getProduct().getName())
                .quantity(item.getQuantity()).priceAtPurchase(item.getPriceAtPurchase()).build())
                .collect(Collectors.toList());
        return OrderResponse.builder().id(order.getId()).status(order.getStatus()).orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount()).items(items).build();
    }
}
