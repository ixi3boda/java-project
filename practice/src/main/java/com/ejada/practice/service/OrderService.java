package com.ejada.practice.service;

import com.ejada.practice.dto.request.OrderRequest;
import com.ejada.practice.dto.request.OrderStatusUpdateRequest;
import com.ejada.practice.dto.response.OrderResponse;
import com.ejada.practice.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service contract for order management.
 * <p>Covers the full order lifecycle: placement, retrieval (by the owning user
 * or by an admin), and status updates.  The implementation is
 * {@link com.ejada.practice.service.impl.OrderServiceImpl}.</p>
 */
public interface OrderService {

    /**
     * Places a new order on behalf of the identified user.
     * <p>Stock quantities are atomically decremented for each ordered product.
     * The order is initially created with status {@link OrderStatus#PENDING}.</p>
     * @param username the username of the authenticated customer
     * @param request  the order payload (list of product IDs and quantities)
     * @return an {@link OrderResponse} representing the persisted order
     * @throws com.ejada.practice.exception.ResourceNotFoundException  if the user or a product is not found
     * @throws com.ejada.practice.exception.InsufficientStockException if a product has insufficient stock
     */
    OrderResponse placeOrder(String username, OrderRequest request);

    /**
     * Returns a paginated list of orders placed by the identified user.
     * @param username the username of the authenticated customer
     * @param pageable pagination and sorting parameters
     * @return a {@link Page} of {@link OrderResponse} objects belonging to the user
     * @throws com.ejada.practice.exception.ResourceNotFoundException if the user is not found
     */
    Page<OrderResponse> getMyOrders(String username, Pageable pageable);

    /**
     * Returns a paginated list of all orders, optionally filtered by status and/or user.
     * @param status   optional filter by {@link OrderStatus}; {@code null} to skip
     * @param userId   optional filter by the owning user's ID; {@code null} to skip
     * @param pageable pagination and sorting parameters
     * @return a filtered and paginated {@link Page} of {@link OrderResponse} objects
     */
    Page<OrderResponse> getAllOrders(OrderStatus status, Long userId, Pageable pageable);

    /**
     * Retrieves a single order by its ID, enforcing ownership for non-admin callers.
     * @param id       the order's primary key
     * @param username the username of the currently authenticated user
     * @param isAdmin  {@code true} if the caller has the {@code ADMIN} role
     * @return an {@link OrderResponse} representing the requested order
     * @throws com.ejada.practice.exception.ResourceNotFoundException if the order is not found
     * @throws org.springframework.security.access.AccessDeniedException if a non-admin user tries to
     *         access an order that does not belong to them
     */
    OrderResponse getOrderById(Long id, String username, boolean isAdmin);

    /**
     * Updates the status of an existing order (admin-only operation).
     * @param id      the order's primary key
     * @param request the new status payload
     * @return an {@link OrderResponse} reflecting the updated status
     * @throws com.ejada.practice.exception.ResourceNotFoundException if the order is not found
     */
    OrderResponse updateStatus(Long id, OrderStatusUpdateRequest request);
}
