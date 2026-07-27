package com.ejada.practice.service.impl;

import com.ejada.practice.dto.request.ProductRequest;
import com.ejada.practice.dto.response.ProductResponse;
import com.ejada.practice.entity.Category;
import com.ejada.practice.entity.Product;
import com.ejada.practice.exception.ResourceNotFoundException;
import com.ejada.practice.repository.CategoryRepository;
import com.ejada.practice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers product CRUD and catalogue filtering.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl")
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category electronics;
    private Product widget;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        electronics = Category.builder().id(1L).name("Electronics").build();
        widget = Product.builder()
                .id(10L)
                .name("Widget")
                .description("A widget")
                .price(new BigDecimal("9.99"))
                .stockQuantity(100)
                .categories(new HashSet<>(Set.of(electronics)))
                .build();
        pageable = PageRequest.of(0, 10);
    }

    /** getAllProducts without a category filter delegates to findAll. */
    @Test
    @DisplayName("getAllProducts without a category filter delegates to findAll")
    void getAllProducts_noCategoryFilter() {
        when(productRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(widget)));

        Page<ProductResponse> result = productService.getAllProducts(null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Widget");
        verify(productRepository).findAll(pageable);
        verify(productRepository, never()).findByCategories_Id(any(), any());
    }

    /** getAllProducts with a category filter delegates to findByCategories_Id. */
    @Test
    @DisplayName("getAllProducts with a category filter delegates to findByCategories_Id")
    void getAllProducts_withCategoryFilter() {
        when(productRepository.findByCategories_Id(1L, pageable)).thenReturn(new PageImpl<>(List.of(widget)));

        Page<ProductResponse> result = productService.getAllProducts(1L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCategories()).containsExactly("Electronics");
        verify(productRepository).findByCategories_Id(1L, pageable);
        verify(productRepository, never()).findAll(pageable);
    }

    /** getProductById returns the mapped product when found. */
    @Test
    @DisplayName("getProductById returns the mapped product when found")
    void getProductById_found() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(widget));

        ProductResponse response = productService.getProductById(10L);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getPrice()).isEqualTo(new BigDecimal("9.99"));
    }

    /** getProductById throws ResourceNotFoundException when missing. */
    @Test
    @DisplayName("getProductById throws ResourceNotFoundException when missing")
    void getProductById_notFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    /** createProduct resolves categories and persists the new product. */
    @Test
    @DisplayName("createProduct resolves categories and persists the new product")
    void createProduct_success() {
        ProductRequest request = new ProductRequest();
        request.setName("Widget");
        request.setDescription("A widget");
        request.setPrice(new BigDecimal("9.99"));
        request.setStockQuantity(100);
        request.setCategoryIds(Set.of(1L));

        when(categoryRepository.findAllById(Set.of(1L))).thenReturn(List.of(electronics));
        when(productRepository.save(any(Product.class))).thenReturn(widget);

        ProductResponse response = productService.createProduct(request);

        assertThat(response.getName()).isEqualTo("Widget");
        assertThat(response.getCategories()).containsExactly("Electronics");
        verify(productRepository).save(any(Product.class));
    }

    /** updateProduct replaces all mutable fields and categories. */
    @Test
    @DisplayName("updateProduct replaces all mutable fields and categories")
    void updateProduct_success() {
        ProductRequest request = new ProductRequest();
        request.setName("Widget v2");
        request.setDescription("Updated");
        request.setPrice(new BigDecimal("19.99"));
        request.setStockQuantity(50);
        request.setCategoryIds(Set.of(1L));

        when(productRepository.findById(10L)).thenReturn(Optional.of(widget));
        when(categoryRepository.findAllById(Set.of(1L))).thenReturn(List.of(electronics));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse response = productService.updateProduct(10L, request);

        assertThat(response.getName()).isEqualTo("Widget v2");
        assertThat(response.getDescription()).isEqualTo("Updated");
        assertThat(response.getPrice()).isEqualTo(new BigDecimal("19.99"));
        assertThat(response.getStockQuantity()).isEqualTo(50);
    }

    /** deleteProduct removes an existing product. */
    @Test
    @DisplayName("deleteProduct removes an existing product")
    void deleteProduct_success() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(widget));

        productService.deleteProduct(10L);

        verify(productRepository, times(1)).delete(widget);
    }

}
