package com.ejada.practice.controller;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.ejada.practice.dto.request.OrderRequest;
import com.ejada.practice.dto.request.OrderStatusUpdateRequest;
import com.ejada.practice.dto.response.OrderResponse;
import com.ejada.practice.entity.OrderStatus;
import com.ejada.practice.service.OrderService;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {
    @Mock private OrderService orderService;
    @Mock private Authentication authentication;
    @InjectMocks private OrderController controller;

    @Test void placeOrderUsesAuthenticatedUsername() {
        var request = new OrderRequest(); 
        var order = new OrderResponse(); 
        when(authentication.getName()).thenReturn("sam"); 
        when(orderService.placeOrder("sam", request)).thenReturn(order);
        assertEquals(201, controller.placeOrder(authentication, request).getStatusCode().value());
    }
    @Test void getMyOrdersUsesAuthenticatedUsername() {
        var pageable = PageRequest.of(0, 10); 
        var page = new PageImpl<>(List.of(new OrderResponse())); 
        when(authentication.getName()).thenReturn("sam"); 
        when(orderService.getMyOrders("sam", pageable)).thenReturn(page);
        assertSame(page, controller.getMyOrders(authentication, pageable).getBody());
    }
    @Test void getAllOrdersReturnsFilteredPage() {
        var pageable = PageRequest.of(0, 10); 
        var page = new PageImpl<>(List.of(new OrderResponse())); 
        when(orderService.getAllOrders(OrderStatus.PENDING, 4L, pageable)).thenReturn(page);
        assertSame(page, controller.getAllOrders(OrderStatus.PENDING, 4L, pageable).getBody());
    }
    @Test void getOrderByIdTreatsAdminAsAdmin() {
        when(authentication.getName()).thenReturn("sam"); 
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities(); 
        when(orderService.getOrderById(7L, "sam", true)).thenReturn(new OrderResponse());
        controller.getOrderById(7L, authentication);
        verify(orderService).getOrderById(7L, "sam", true);
    }
    @Test void getOrderByIdTreatsUserAsNonAdmin() {
        when(authentication.getName()).thenReturn("sam"); 
        doReturn(Set.of(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities(); 
        when(orderService.getOrderById(7L, "sam", false)).thenReturn(new OrderResponse());
        controller.getOrderById(7L, authentication);
        verify(orderService).getOrderById(7L, "sam", false);
    }
    @Test void updateStatusReturnsUpdatedOrder() {
        var request = new OrderStatusUpdateRequest(); 
        var order = new OrderResponse(); 
        when(orderService.updateStatus(7L, request)).thenReturn(order);
        assertSame(order, controller.updateStatus(7L, request).getBody());
    }
}
