package com.ejada.practice.service.impl;

import com.ejada.practice.dto.request.OrderItemRequest;
import com.ejada.practice.dto.request.OrderRequest;
import com.ejada.practice.dto.request.OrderStatusUpdateRequest;
import com.ejada.practice.entity.*;
import com.ejada.practice.exception.InsufficientStockException;
import com.ejada.practice.repository.OrderRepository;
import com.ejada.practice.repository.ProductRepository;
import com.ejada.practice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    @Mock OrderRepository orders; 
    @Mock ProductRepository products; 
    @Mock UserRepository users; 
    @InjectMocks OrderServiceImpl service;

    @Test void placesOrderAndReducesStock() {
        User user = User.builder().id(1L).username("ann").build(); 
        Product product = Product.builder().id(2L).name("Book").price(new BigDecimal("5.00")).stockQuantity(3).build();
        when(users.findByUsername("ann")).thenReturn(Optional.of(user)); 
        when(products.findById(2L)).thenReturn(Optional.of(product));
        when(orders.save(any())).thenAnswer(i -> i.getArgument(0));
        OrderItemRequest item = new OrderItemRequest(); 
        item.setProductId(2L); 
        item.setQuantity(2); 
        OrderRequest request = new OrderRequest(); 
        request.setItems(List.of(item));
        var response = service.placeOrder("ann", request);
        assertEquals(new BigDecimal("10.00"), response.getTotalAmount()); assertEquals(1, product.getStockQuantity()); 
        verify(products).save(product);
    }
    @Test void rejectsOrderWhenStockIsInsufficient() {
        when(users.findByUsername("ann")).thenReturn(Optional.of(User.builder().username("ann").build()));
        when(products.findById(2L)).thenReturn(Optional.of(Product.builder().name("Book").stockQuantity(0).build()));
        OrderItemRequest item = new OrderItemRequest(); 
        item.setProductId(2L); 
        item.setQuantity(1); 
        OrderRequest request = new OrderRequest(); 
        request.setItems(List.of(item));
        assertThrows(InsufficientStockException.class, () -> service.placeOrder("ann", request));
    }
    @Test void updatesOrderStatus() {
        Order order = Order.builder().items(List.of()).status(OrderStatus.PENDING).build(); when(orders.findById(1L)).thenReturn(Optional.of(order)); 
        when(orders.save(order)).thenReturn(order);
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest(); 
        request.setStatus(OrderStatus.SHIPPED);
        assertEquals(OrderStatus.SHIPPED, service.updateStatus(1L, request).getStatus());
    }
}
