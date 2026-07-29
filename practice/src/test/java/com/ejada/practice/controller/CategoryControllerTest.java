package com.ejada.practice.controller;

import com.ejada.practice.dto.request.CategoryRequest;
import com.ejada.practice.dto.response.CategoryResponse;
import com.ejada.practice.exception.DuplicateResourceException;
import com.ejada.practice.exception.GlobalExceptionHandler;
import com.ejada.practice.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Covers category listing and creation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryController")
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        CategoryController controller = new CategoryController(categoryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /** GET /api/categories returns 200 with the full list. */
    @Test
    @DisplayName("GET /api/categories returns 200 with the full list")
    void getAllCategories_returns200() throws Exception {
        when(categoryService.getAllCategories())
                .thenReturn(List.of(CategoryResponse.builder().id(1L).name("Electronics").build()));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Electronics"));
    }

    /** POST /api/categories returns 201 with the created category. */
    @Test
    @DisplayName("POST /api/categories returns 201 with the created category")
    void createCategory_returns201() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Electronics");

        when(categoryService.createCategory(any(CategoryRequest.class)))
                .thenReturn(CategoryResponse.builder().id(1L).name("Electronics").build());

        mockMvc.perform(post("/api/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    /** POST /api/categories returns 409 when the category name already exists. */
    @Test
    @DisplayName("POST /api/categories returns 409 when the category name already exists")
    void createCategory_duplicate_returns409() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Electronics");

        when(categoryService.createCategory(any(CategoryRequest.class)))
                .thenThrow(new DuplicateResourceException("Category already exists: Electronics"));

        mockMvc.perform(post("/api/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    /** POST /api/categories returns 400 when the name is blank. */
    @Test
    @DisplayName("POST /api/categories returns 400 when the name is blank")
    void createCategory_blankName_returns400() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("");

        mockMvc.perform(post("/api/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
