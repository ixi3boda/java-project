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

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public Page<ProductResponse> getAllProducts(Long categoryId, Pageable pageable) {
        Page<Product> products = categoryId == null ? productRepository.findAll(pageable)
                : productRepository.findByCategories_Id(categoryId, pageable);
        return products.map(this::toResponse);
    }

    @Override
    public ProductResponse getProductById(Long id) {
        return toResponse(findById(id));
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = Product.builder().name(request.getName()).description(request.getDescription())
                .price(request.getPrice()).stockQuantity(request.getStockQuantity())
                .categories(resolveCategories(request.getCategoryIds())).build();
        return toResponse(productRepository.save(product));
    }

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

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        productRepository.delete(findById(id));
    }

    private Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    private Set<Category> resolveCategories(Set<Long> categoryIds) {
        return categoryIds.stream().map(id -> categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id)))
                .collect(Collectors.toSet());
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder().id(product.getId()).name(product.getName())
                .description(product.getDescription()).price(product.getPrice()).stockQuantity(product.getStockQuantity())
                .categories(product.getCategories().stream().map(Category::getName).collect(Collectors.toSet())).build();
    }
}
