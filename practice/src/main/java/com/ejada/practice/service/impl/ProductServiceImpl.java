package com.ejada.practice.service.impl;

import com.ejada.practice.dto.request.ProductRequest;
import com.ejada.practice.dto.response.ProductResponse;
import com.ejada.practice.entity.Category;
import com.ejada.practice.entity.Product;
import com.ejada.practice.exception.ResourceNotFoundException;
import com.ejada.practice.repository.CategoryRepository;
import com.ejada.practice.repository.ProductRepository;
import com.ejada.practice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link ProductService}.
 *
 * <p>Manages the full lifecycle of products in the catalogue: listing,
 * individual retrieval, creation, updates, and deletion.  Category resolution
 * is performed by bulk-fetching all requested category IDs in one query and
 * verifying that the returned count matches the requested set.</p>
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * {@inheritDoc}
     *
     * <p>When {@code categoryId} is provided the repository's derived query
     * ({@code findByCategories_Id}) is used; otherwise {@code findAll} with
     * paging is used directly.</p>
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Long categoryId, Pageable pageable) {
        Page<Product> products = categoryId != null
                ? productRepository.findByCategories_Id(categoryId, pageable)
                : productRepository.findAll(pageable);
        return products.map(this::toResponse);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates to the shared {@link #findById(Long)} helper which throws
     * {@link ResourceNotFoundException} on miss.</p>
     */
    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return toResponse(findById(id));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Categories are resolved via {@link #resolveCategories(Set)} before
     * the product is built and persisted.</p>
     */
    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .categories(resolveCategories(request.getCategoryIds()))
                .build();

        return toResponse(productRepository.save(product));
    }

    /**
     * {@inheritDoc}
     *
     * <p>All mutable fields (name, description, price, stock, categories) are
     * replaced atomically within the transaction.</p>
     */
    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findById(id);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategories(resolveCategories(request.getCategoryIds()));

        return toResponse(productRepository.save(product));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Uses {@link #findById(Long)} to surface a friendly error before
     * calling {@code delete}.</p>
     */
    @Override
    @Transactional
    public void deleteProduct(Long id) {
        productRepository.delete(findById(id));
    }

    /**
     * Retrieves a {@link Product} by its primary key, throwing
     * {@link ResourceNotFoundException} if it does not exist.
     *
     * @param id the product's primary key
     * @return the found {@link Product} entity
     * @throws ResourceNotFoundException if no product with the given ID exists
     */
    private Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    /**
     * Resolves a set of category IDs to {@link Category} entities.
     *
     * <p>All IDs are fetched in one query.  If the returned count does not match
     * the requested count, at least one ID was invalid and a
     * {@link ResourceNotFoundException} is thrown.</p>
     *
     * @param categoryIds the set of category IDs to resolve
     * @return the corresponding set of {@link Category} entities
     * @throws ResourceNotFoundException if any ID does not correspond to an existing category
     */
    private Set<Category> resolveCategories(Set<Long> categoryIds) {
        Set<Category> categories = new HashSet<>(categoryRepository.findAllById(categoryIds));
        if (categories.size() != categoryIds.size()) {
            throw new ResourceNotFoundException("One or more categories not found");
        }
        return categories;
    }

    /**
     * Converts a {@link Product} entity to a {@link ProductResponse} DTO.
     *
     * @param product the entity to convert; must not be {@code null}
     * @return the corresponding response DTO
     */
    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .categories(product.getCategories().stream().map(Category::getName).collect(Collectors.toSet()))
                .build();
    }
}
