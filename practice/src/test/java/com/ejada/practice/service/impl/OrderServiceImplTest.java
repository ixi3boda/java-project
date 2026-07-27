package com.ejada.practice.service.impl;

import com.ejada.practice.dto.request.OrderItemRequest;
import com.ejada.practice.dto.request.OrderRequest;
import com.ejada.practice.dto.request.OrderStatusUpdateRequest;
import com.ejada.practice.dto.response.OrderResponse;
import com.ejada.practice.entity.Order;
import com.ejada.practice.entity.OrderItem;
import com.ejada.practice.entity.OrderStatus;
import com.ejada.practice.entity.Product;
import com.ejada.practice.entity.User;
import com.ejada.practice.exception.InsufficientStockException;
import com.ejada.practice.exception.ResourceNotFoundException;
import com.ejada.practice.repository.OrderRepository;
import com.ejada.practice.repository.ProductRepository;
import com.ejada.practice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers order placement, retrieval, and status transitions.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl")
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User user;
    private Product product;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("alice").build();
        product = Product.builder()
                .id(100L)
                .name("Widget")
                .price(new BigDecimal("10.00"))
                .stockQuantity(5)
                .build();
        pageable = PageRequest.of(0, 10);
    }

    private OrderRequest requestFor(long productId, int quantity) {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(productId);
        item.setQuantity(quantity);
        OrderRequest request = new OrderRequest();
        request.setItems(List.of(item));
        return request;
    }

    /** placeOrder decrements stock, computes the total, and saves the order. */
    @Test
    @DisplayName("placeOrder decrements stock, computes the total, and saves the order")
    void placeOrder_success() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order order = inv.getArgument(0);
            order.setId(500L);
            return order;
        });

        OrderResponse response = orderService.placeOrder("alice", requestFor(100L, 2));

        assertThat(response.getId()).isEqualTo(500L);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.getTotalAmount()).isEqualByComparingTo("20.00");
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getProductId()).isEqualTo(100L);
        assertThat(response.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(product.getStockQuantity()).isEqualTo(3);
        verify(productRepository).save(product);
    }

    /** placeOrder throws ResourceNotFoundException when the user does not exist. */
    @Test
    @DisplayName("placeOrder throws ResourceNotFoundException when the user does not exist")
    void placeOrder_userNotFound_throws() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder("ghost", requestFor(100L, 1)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ghost");
        verify(orderRepository, never()).save(any());
    }

    /** placeOrder throws InsufficientStockException when stock is too low. */
    @Test
    @DisplayName("placeOrder throws InsufficientStockException when stock is too low")
    void placeOrder_insufficientStock_throws() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.placeOrder("alice", requestFor(100L, 10)))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Widget");
        verify(orderRepository, never()).save(any());
    }

    /** getMyOrders resolves the user then delegates to findByUserId. */
    @Test
    @DisplayName("getMyOrders resolves the user then delegates to findByUserId")
    void getMyOrders_success() {
        Order order = sampleOrder(1L, user, OrderStatus.PENDING);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(orderRepository.findByUserId(1L, pageable)).thenReturn(new PageImpl<>(List.of(order)));

        Page<OrderResponse> result = orderService.getMyOrders("alice", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
    }

    /** getAllOrders filters by status and user when both are provided. */
    @Test
    @DisplayName("getAllOrders filters by status and user when both are provided")
    void getAllOrders_statusAndUser() {
        Order order = sampleOrder(1L, user, OrderStatus.CONFIRMED);
        when(orderRepository.findByUserIdAndStatus(1L, OrderStatus.CONFIRMED, pageable))
                .thenReturn(new PageImpl<>(List.of(order)));

        Page<OrderResponse> result = orderService.getAllOrders(OrderStatus.CONFIRMED, 1L, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(orderRepository).findByUserIdAndStatus(1L, OrderStatus.CONFIRMED, pageable);
    }

    /** getAllOrders returns everything when no filter is provided. */
    @Test
    @DisplayName("getAllOrders returns everything when no filter is provided")
    void getAllOrders_noFilter() {
        Order order = sampleOrder(1L, user, OrderStatus.PENDING);
        when(orderRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(order)));

        Page<OrderResponse> result = orderService.getAllOrders(null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(orderRepository).findAll(pageable);
    }

    /** getOrderById returns the order for its owner. */
    @Test
    @DisplayName("getOrderById returns the order for its owner")
    void getOrderById_ownerAccess() {
        Order order = sampleOrder(1L, user, OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(1L, "alice", false);

        assertThat(response.getId()).isEqualTo(1L);
    }

    /** getOrderById returns the order for an admin regardless of ownership. */
    @Test
    @DisplayName("getOrderById returns the order for an admin regardless of ownership")
    void getOrderById_adminAccess() {
        Order order = sampleOrder(1L, user, OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(1L, "someone-else", true);

        assertThat(response.getId()).isEqualTo(1L);
    }

    /** getOrderById throws AccessDeniedException for a non-owner, non-admin caller. */
    @Test
    @DisplayName("getOrderById throws AccessDeniedException for a non-owner, non-admin caller")
    void getOrderById_forbidden_throws() {
        Order order = sampleOrder(1L, user, OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getOrderById(1L, "bob", false))
                .isInstanceOf(AccessDeniedException.class);
    }

    /** updateStatus persists the new status and returns the updated order. */
    @Test
    @DisplayName("updateStatus persists the new status and returns the updated order")
    void updateStatus_success() {
        Order order = sampleOrder(1L, user, OrderStatus.PENDING);
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
        request.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        OrderResponse response = orderService.updateStatus(1L, request);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        verify(orderRepository, times(1)).save(order);
    }

    private Order sampleOrder(Long id, User owner, OrderStatus status) {
        Order order = Order.builder()
                .id(id)
                .user(owner)
                .status(status)
                .totalAmount(new BigDecimal("20.00"))
                .items(new ArrayList<>())
                .build();
        OrderItem item = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(2)
                .priceAtPurchase(new BigDecimal("10.00"))
                .build();
        order.getItems().add(item);
        return order;
    }
}
