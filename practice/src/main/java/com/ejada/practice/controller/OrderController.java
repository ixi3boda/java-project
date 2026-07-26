package com.ejada.practice.controller;

import com.ejada.practice.dto.request.OrderRequest;
import com.ejada.practice.dto.request.OrderStatusUpdateRequest;
import com.ejada.practice.dto.response.OrderResponse;
import com.ejada.practice.entity.OrderStatus;
import com.ejada.practice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing customer orders.
 * <p>Role-based access rules:
 * <ul>
 *   <li>Placing an order and viewing own orders → {@code USER} role</li>
 *   <li>Listing all orders and updating order status → {@code ADMIN} role</li>
 *   <li>Fetching a single order → any authenticated user (ownership is
 *       enforced at the service layer for non-admins)</li>
 * </ul>
 * </p>
 * <p>Base path: {@code /api/orders}</p>
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    /** Service that encapsulates order business logic. */
    private final OrderService orderService;

    /**
     * Places a new order on behalf of the authenticated user.
     *
     * <p>Stock is decremented atomically for each requested product.
     * Returns {@code 400 Bad Request} if any product has insufficient stock.</p>
     *
     * @param authentication the current authenticated user (injected by Spring Security)
     * @param request        the order payload (list of product IDs and quantities)
     * @return {@code 201 Created} containing the newly created order
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderResponse> placeOrder(Authentication authentication,
                                                      @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.placeOrder(authentication.getName(), request));
    }

    /**
     * Returns a paginated list of orders belonging to the authenticated user.
     *
     * @param authentication the current authenticated user
     * @param pageable       pagination and sorting parameters
     * @return {@code 200 OK} containing a page of the user's orders
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<OrderResponse>> getMyOrders(Authentication authentication, Pageable pageable) {
        return ResponseEntity.ok(orderService.getMyOrders(authentication.getName(), pageable));
    }

    /**
     * Returns a paginated list of all orders (admin view), with optional filters.
     *
     * @param status   optional filter by {@link OrderStatus}
     * @param userId   optional filter by the owning user's ID
     * @param pageable pagination and sorting parameters
     * @return {@code 200 OK} containing a filtered/sorted page of orders
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(orderService.getAllOrders(status, userId, pageable));
    }

    /**
     * Retrieves a single order by its ID.
     * Returns {@code 403 Forbidden} if a non-admin tries to access another user's order.</p>
     * @param id             the order's primary key
     * @param authentication the current authenticated user
     * @return {@code 200 OK} containing the order details
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(orderService.getOrderById(id, authentication.getName(), isAdmin));
    }

    /**
     * Updates the status of an existing order (admin only).
     * @param id      the order's primary key
     * @param request the new status payload
     * @return {@code 200 OK} containing the updated order
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id,
                                                        @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(id, request));
    }
}
