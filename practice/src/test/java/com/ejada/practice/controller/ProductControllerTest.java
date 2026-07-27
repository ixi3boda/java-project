package com.ejada.practice.controller;

import com.ejada.practice.dto.request.ProductRequest;
import com.ejada.practice.dto.response.ProductResponse;
import com.ejada.practice.exception.GlobalExceptionHandler;
import com.ejada.practice.exception.ResourceNotFoundException;
import com.ejada.practice.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import static org.mockito.ArgumentMatchers.nullable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Covers the product catalogue endpoints.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController")
class ProductControllerTest {

    @Mock
    private ProductService productService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ProductController controller = new ProductController(productService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    private ProductResponse sampleProduct() {
        return ProductResponse.builder()
                .id(1L).name("Widget").description("desc")
                .price(new BigDecimal("9.99")).stockQuantity(10)
                .categories(Set.of("Electronics"))
                .build();
    }

    /** GET /api/products returns 200 with a page of products. */
    @Test
    @DisplayName("GET /api/products returns 200 with a page of products")
    void getAllProducts_returns200() throws Exception {
        when(productService.getAllProducts(nullable(Long.class), any()))
                .thenReturn(new PageImpl<>(List.of(sampleProduct()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Widget"));
    }

    /** GET /api/products?categoryId=1 filters by category. */
    @Test
    @DisplayName("GET /api/products?categoryId=1 filters by category")
    void getAllProducts_withCategoryFilter_returns200() throws Exception {
        when(productService.getAllProducts(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of(sampleProduct()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/products").param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    /** GET /api/products/{id} returns 200 with the product. */
    @Test
    @DisplayName("GET /api/products/{id} returns 200 with the product")
    void getProductById_returns200() throws Exception {
        when(productService.getProductById(1L)).thenReturn(sampleProduct());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Widget"));
    }

    /** GET /api/products/{id} returns 404 when not found. */
    @Test
    @DisplayName("GET /api/products/{id} returns 404 when not found")
    void getProductById_notFound_returns404() throws Exception {
        when(productService.getProductById(99L)).thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound());
    }

    private ProductRequest sampleRequest() {
        ProductRequest request = new ProductRequest();
        request.setName("Widget");
        request.setDescription("desc");
        request.setPrice(new BigDecimal("9.99"));
        request.setStockQuantity(10);
        request.setCategoryIds(Set.of(1L));
        return request;
    }

    /** POST /api/products returns 201 with the created product. */
    @Test
    @DisplayName("POST /api/products returns 201 with the created product")
    void createProduct_returns201() throws Exception {
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(sampleProduct());

        mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isCreated());
    }

    /** POST /api/products returns 400 for an invalid payload. */
    @Test
    @DisplayName("POST /api/products returns 400 for an invalid payload")
    void createProduct_invalidPayload_returns400() throws Exception {
        ProductRequest request = new ProductRequest();

        mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /** PUT /api/products/{id} returns 200 with the updated product. */
    @Test
    @DisplayName("PUT /api/products/{id} returns 200 with the updated product")
    void updateProduct_returns200() throws Exception {
        when(productService.updateProduct(eq(1L), any(ProductRequest.class))).thenReturn(sampleProduct());

        mockMvc.perform(put("/api/products/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isOk());
    }

    /** DELETE /api/products/{id} returns 204. */
    @Test
    @DisplayName("DELETE /api/products/{id} returns 204")
    void deleteProduct_returns204() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }
}
