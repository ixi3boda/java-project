package com.ejada.practice.controller;

import com.ejada.practice.dto.request.OrderItemRequest;
import com.ejada.practice.dto.request.OrderRequest;
import com.ejada.practice.dto.request.OrderStatusUpdateRequest;
import com.ejada.practice.dto.response.OrderResponse;
import com.ejada.practice.entity.OrderStatus;
import com.ejada.practice.exception.GlobalExceptionHandler;
import com.ejada.practice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Covers order placement, listing, and status updates.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderController")
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        OrderController controller = new OrderController(orderService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver()
                )
                .build();
    }

    private OrderResponse sampleOrder() {
        return OrderResponse.builder().id(1L).status(OrderStatus.PENDING).items(List.of()).build();
    }

    /** POST /api/orders places an order for the authenticated user and returns 201. */
    @Test
    @DisplayName("POST /api/orders places an order for the authenticated user and returns 201")
    void placeOrder_returns201() throws Exception {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(2);
        OrderRequest request = new OrderRequest();
        request.setItems(List.of(item));

        when(orderService.placeOrder(eq("alice"), any(OrderRequest.class))).thenReturn(sampleOrder());

        mockMvc.perform(post("/api/orders")
                        .principal(new UsernamePasswordAuthenticationToken("alice", null, List.of()))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    /** GET /api/orders/my returns the authenticated user's orders. */
    @Test
    @DisplayName("GET /api/orders/my returns the authenticated user's orders")
    void getMyOrders_returns200() throws Exception {
        when(orderService.getMyOrders(eq("alice"), any()))
                .thenReturn(new PageImpl<>(List.of(sampleOrder()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/orders/my")
                        .principal(new UsernamePasswordAuthenticationToken("alice", null, List.of())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    /** GET /api/orders (admin) returns all orders with no filters. */
    @Test
    @DisplayName("GET /api/orders (admin) returns all orders with no filters")
    void getAllOrders_noFilters_returns200() throws Exception {
        when(orderService.getAllOrders(
                nullable(OrderStatus.class),
                nullable(Long.class),
                any()))
                .thenReturn(new PageImpl<>(List.of(sampleOrder()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk());
    }

    /** GET /api/orders/{id} returns the order for an admin caller. */
    @Test
    @DisplayName("GET /api/orders/{id} returns the order for an admin caller")
    void getOrderById_admin_returns200() throws Exception {
        when(orderService.getOrderById(eq(1L), eq("admin"), eq(true))).thenReturn(sampleOrder());

        mockMvc.perform(get("/api/orders/1")
                        .principal(new UsernamePasswordAuthenticationToken(
                                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))))
                .andExpect(status().isOk());
    }

    /** GET /api/orders/{id} returns 403 when a non-admin requests someone else's order. */
    @Test
    @DisplayName("GET /api/orders/{id} returns 403 when a non-admin requests someone else's order")
    void getOrderById_forbidden_returns403() throws Exception {
        when(orderService.getOrderById(eq(1L), eq("bob"), eq(false)))
                .thenThrow(new AccessDeniedException("forbidden"));

        mockMvc.perform(get("/api/orders/1")
                        .principal(new UsernamePasswordAuthenticationToken("bob", null, List.of())))
                .andExpect(status().isForbidden());
    }

    /** PATCH /api/orders/{id}/status updates the order status. */
    @Test
    @DisplayName("PATCH /api/orders/{id}/status updates the order status")
    void updateStatus_returns200() throws Exception {
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
        request.setStatus(OrderStatus.SHIPPED);

        OrderResponse shipped = OrderResponse.builder().id(1L).status(OrderStatus.SHIPPED).items(List.of()).build();
        when(orderService.updateStatus(eq(1L), any(OrderStatusUpdateRequest.class))).thenReturn(shipped);

        mockMvc.perform(patch("/api/orders/1/status")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }
}
