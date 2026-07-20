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

    }

    @Override
    public ProductResponse getProductById(Long id) {

    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {

    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {

    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {

    }

    private Product findById(Long id) {

    }

    private Set<Category> resolveCategories(Set<Long> categoryIds) {

    }

    private ProductResponse toResponse(Product product) {

    }
}
