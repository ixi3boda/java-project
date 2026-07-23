package com.ejada.practice.controller;

import com.ejada.practice.dto.request.ProductRequest;
import com.ejada.practice.dto.response.ProductResponse;
import com.ejada.practice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {
    @Mock private ProductService productService;
    @InjectMocks private ProductController controller;

    @Test void getAllProductsReturnsPage() {
        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(new ProductResponse()));
        when(productService.getAllProducts(2L, pageable)).thenReturn(page);
        assertSame(page, controller.getAllProducts(2L, pageable).getBody());
    }

    @Test void getProductByIdReturnsProduct() {
        var product = ProductResponse.builder().id(3L).build();
        when(productService.getProductById(3L)).thenReturn(product);
        assertSame(product, controller.getProductById(3L).getBody());
    }

    @Test void createProductReturnsCreatedProduct() {
        ProductRequest request = new ProductRequest();
        var product = new ProductResponse();
        when(productService.createProduct(request)).thenReturn(product);
        assertEquals(201, controller.createProduct(request).getStatusCode().value());
    }

    @Test void updateProductReturnsUpdatedProduct() {
        ProductRequest request = new ProductRequest();
        var product = new ProductResponse();
        when(productService.updateProduct(3L, request)).thenReturn(product);
        assertSame(product, controller.updateProduct(3L, request).getBody());
    }

    @Test void deleteProductReturnsNoContent() {
        assertEquals(204, controller.deleteProduct(3L).getStatusCode().value());
        verify(productService).deleteProduct(3L);
    }
}
