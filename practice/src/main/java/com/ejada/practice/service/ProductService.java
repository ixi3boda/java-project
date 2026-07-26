package com.ejada.practice.service;

import com.ejada.practice.dto.request.ProductRequest;
import com.ejada.practice.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service contract for product catalogue management.
 * <p>Provides CRUD operations on products, including optional filtering by
 * category.  The implementation is
 * {@link com.ejada.practice.service.impl.ProductServiceImpl}.</p>
 */
public interface ProductService {

    /**
     * Returns a paginated list of products, optionally filtered by category.
     * @param categoryId the ID of the category to filter by; pass {@code null} to retrieve all products
     * @param pageable   pagination and sorting parameters
     * @return a {@link Page} of {@link ProductResponse} objects
     */
    Page<ProductResponse> getAllProducts(Long categoryId, Pageable pageable);

    /**
     * Retrieves a single product by its primary key.
     * @param id the product's primary key
     * @return a {@link ProductResponse} representing the product
     * @throws com.ejada.practice.exception.ResourceNotFoundException if no product with the given ID exists
     */
    ProductResponse getProductById(Long id);

    /**
     * Creates a new product in the catalogue.
     * @param request the creation payload (name, description, price, stock, category IDs)
     * @return a {@link ProductResponse} representing the newly persisted product
     * @throws com.ejada.practice.exception.ResourceNotFoundException if any of the specified
     *         category IDs do not correspond to an existing category
     */
    ProductResponse createProduct(ProductRequest request);

    /**
     * Fully replaces the data of an existing product.
     * @param id      the product's primary key
     * @param request the updated product payload
     * @return a {@link ProductResponse} reflecting the changes
     * @throws com.ejada.practice.exception.ResourceNotFoundException if the product or any
     *         specified category is not found
     */
    ProductResponse updateProduct(Long id, ProductRequest request);

    /**
     * Permanently deletes a product from the catalogue.
     * @param id the product's primary key
     * @throws com.ejada.practice.exception.ResourceNotFoundException if no product with the given ID exists
     */
    void deleteProduct(Long id);
}
