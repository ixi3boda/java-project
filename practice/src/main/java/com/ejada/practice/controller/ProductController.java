package com.ejada.practice.controller;

import com.ejada.practice.dto.request.ProductRequest;
import com.ejada.practice.dto.response.ProductResponse;
import com.ejada.practice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for the product catalogue.
 *
 * <p>Reading products is publicly accessible (no authentication required).
 * All write operations (create, update, delete) are restricted to the
 * {@code ADMIN} role.</p>
 *
 * <p>Base path: {@code /api/products}</p>
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    /** Service that encapsulates product business logic. */
    private final ProductService productService;

    /**
     * Returns a paginated list of products, optionally filtered by category.
     * @param categoryId optional category ID to filter by; all products are returned if omitted
     * @param pageable   pagination and sorting parameters
     * @return {@code 200 OK} containing a page of products
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(required = false) Long categoryId,
            Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(categoryId, pageable));
    }

    /**
     * Retrieves a single product by its ID.
     * <p>Returns {@code 404 Not Found} if no product exists with the given ID.</p>
     * @param id the product's primary key
     * @return {@code 200 OK} containing the product details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * Creates a new product in the catalogue.
     * Returns {@code 404 Not Found} if any of the specified category IDs do not exist.</p>
     * @param request the product creation payload (name, description, price, stock, categories)
     * @return {@code 201 Created} containing the newly created product
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    /**
     * Fully replaces an existing product's data.
     * Returns {@code 404 Not Found} if the product or any category ID is not found.</p>
     * @param id      the product's primary key
     * @param request the updated product payload
     * @return {@code 200 OK} containing the updated product
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                          @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    /**
     * Permanently deletes a product from the catalogue.
     * Returns {@code 404 Not Found} if no product exists with the given ID.</p>
     * @param id the product's primary key
     * @return {@code 204 No Content} on successful deletion
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
