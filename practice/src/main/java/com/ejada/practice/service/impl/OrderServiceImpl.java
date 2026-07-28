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

/**
 * Default implementation of {@link OrderService}.
 *
 * <p>Key design decisions:
 * <ul>
 *   <li>Stock is decremented synchronously within the placement transaction;
 *       if any product has insufficient stock the whole transaction rolls back.</li>
 *   <li>The unit price at the time of purchase is captured in
 *       {@link OrderItem#getPriceAtPurchase()} so historical totals remain
 *       accurate even after a product's price changes.</li>
 *   <li>Ownership enforcement for {@link #getOrderById} is done at the
 *       service layer rather than the controller to keep the controller thin.</li>
 * </ul>
 * </p>
 */

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * {@inheritDoc}
     *
     * <p>Iterates over each requested item, validates stock, decrements inventory,
     * accumulates the running total, and saves the {@link Order} together with all
     * {@link OrderItem}s in a single transaction.</p>
     */
    @Override
    @Transactional
    public OrderResponse placeOrder(String username, OrderRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        Order order = Order.builder()
                .user(user)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .items(orderItems)
                .build();

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + itemRequest.getProductId()));

            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }

            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
            productRepository.save(product);

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            total = total.add(lineTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .build();
            orderItems.add(orderItem);
        }

        order.setTotalAmount(total);

        return toResponse(orderRepository.save(order));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Resolves the user by username before querying the order repository
     * so a meaningful error is returned if the account no longer exists.</p>
     */
    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return orderRepository.findByUserId(user.getId(), pageable).map(this::toResponse);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Applies filter combinations:
     * <ul>
     *   <li>Both {@code status} and {@code userId} provided → filter by both.</li>
     *   <li>Only {@code status} provided → filter by status only.</li>
     *   <li>Only {@code userId} provided → filter by user only.</li>
     *   <li>Neither provided → return all orders.</li>
     * </ul>
     * </p>
     */
    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(OrderStatus status, Long userId, Pageable pageable) {
        Page<Order> orders;
        if (status != null && userId != null) {
            orders = orderRepository.findByUserIdAndStatus(userId, status, pageable);
        } else if (status != null) {
            orders = orderRepository.findByStatus(status, pageable);
        } else if (userId != null) {
            orders = orderRepository.findByUserId(userId, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }
        return orders.map(this::toResponse);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Non-admin users may only view their own orders; an
     * {@link AccessDeniedException} is thrown otherwise.</p>
     */
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id, String username, boolean isAdmin) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        if (!isAdmin && !order.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not have permission to view this order");
        }

        return toResponse(order);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Persists the new status and returns the updated order representation.</p>
     */
    @Override
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        order.setStatus(request.getStatus());
        return toResponse(orderRepository.save(order));
    }

    /**
     * Converts an {@link Order} entity (including its items) to an {@link OrderResponse} DTO.
     *
     * @param order the entity to convert; must not be {@code null}
     * @return the fully populated response DTO
     */
    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .priceAtPurchase(item.getPriceAtPurchase())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .items(items)
                .build();
    }

    @Override
    @Transactional
    public OrderResponse placeOrder(String username, OrderRequest request) {

    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(String username, Pageable pageable) {

    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(OrderStatus status, Long userId, Pageable pageable) {

    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id, String username, boolean isAdmin) {

    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatusUpdateRequest request) {

    }

    private OrderResponse toResponse(Order order) {

    }

}
