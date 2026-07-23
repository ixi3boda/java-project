package com.ejada.practice.controller;

import com.ejada.practice.dto.request.CategoryRequest;
import com.ejada.practice.dto.response.CategoryResponse;
import com.ejada.practice.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {
    @Mock private CategoryService categoryService;
    @InjectMocks private CategoryController controller;

    @Test void getAllCategoriesReturnsServiceResults() {
        var categories = List.of(CategoryResponse.builder().name("Books").build());
        when(categoryService.getAllCategories()).thenReturn(categories);

        assertSame(categories, controller.getAllCategories().getBody());
        verify(categoryService).getAllCategories();
    }

    @Test void createCategoryReturnsCreatedCategory() {
        CategoryRequest request = new CategoryRequest();
        CategoryResponse category = CategoryResponse.builder().name("Books").build();
        when(categoryService.createCategory(request)).thenReturn(category);

        var response = controller.createCategory(request);

        assertEquals(201, response.getStatusCode().value());
        assertSame(category, response.getBody());
        verify(categoryService).createCategory(request);
    }
}
