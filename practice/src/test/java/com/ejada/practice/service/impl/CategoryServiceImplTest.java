package com.ejada.practice.service.impl;

import com.ejada.practice.dto.request.CategoryRequest;
import com.ejada.practice.dto.response.CategoryResponse;
import com.ejada.practice.entity.Category;
import com.ejada.practice.exception.DuplicateResourceException;
import com.ejada.practice.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers category creation and duplicate-name handling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceImpl")
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category electronics;

    @BeforeEach
    void setUp() {
        electronics = Category.builder().id(1L).name("Electronics").build();
    }

    /** getAllCategories maps every entity to a response DTO. */
    @Test
    @DisplayName("getAllCategories maps every entity to a response DTO")
    void getAllCategories_returnsMappedList() {
        Category books = Category.builder().id(2L).name("Books").build();
        when(categoryRepository.findAll()).thenReturn(List.of(electronics, books));

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CategoryResponse::getId).containsExactly(1L, 2L);
        assertThat(result).extracting(CategoryResponse::getName).containsExactly("Electronics", "Books");
    }

    /** getAllCategories returns an empty list when there are no categories. */
    @Test
    @DisplayName("getAllCategories returns an empty list when there are no categories")
    void getAllCategories_empty() {
        when(categoryRepository.findAll()).thenReturn(List.of());

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertThat(result).isEmpty();
    }

    /** createCategory persists and returns the new category when the name is unique. */
    @Test
    @DisplayName("createCategory persists and returns the new category when the name is unique")
    void createCategory_success() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Electronics");

        when(categoryRepository.findByName("Electronics")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(electronics);

        CategoryResponse response = categoryService.createCategory(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Electronics");
        verify(categoryRepository).save(any(Category.class));
    }

    /** createCategory throws DuplicateResourceException when the name already exists. */
    @Test
    @DisplayName("createCategory throws DuplicateResourceException when the name already exists")
    void createCategory_duplicateName_throws() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Electronics");

        when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(electronics));

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Electronics");
    }
}
