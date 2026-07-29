package com.ejada.practice.service.impl;

import com.ejada.practice.dto.request.CategoryRequest;
import com.ejada.practice.dto.response.CategoryResponse;
import com.ejada.practice.entity.Category;
import com.ejada.practice.exception.DuplicateResourceException;
import com.ejada.practice.repository.CategoryRepository;
import com.ejada.practice.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link CategoryService}.
 *
 * <p>Provides read and write access to the {@code categories} table via
 * {@link CategoryRepository}.  Duplicate category names are rejected before
 * insertion to preserve the unique constraint.</p>
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * {@inheritDoc}
     *
     * <p>Fetches all rows from the {@code categories} table and maps them to
     * response DTOs.  The query runs in a read-only transaction for optimal
     * performance.</p>
     */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Checks for a name collision before persisting to provide a friendly
     * error message instead of letting the database unique constraint fire.</p>
     */
    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.findByName(request.getName()).isPresent()) {
            throw new DuplicateResourceException("Category already exists: " + request.getName());
        }

        Category category = Category.builder()
                .name(request.getName())
                .build();

        return toResponse(categoryRepository.save(category));
    }

    /**
     * Converts a {@link Category} entity to a {@link CategoryResponse} DTO.
     *
     * @param category the entity to convert; must not be {@code null}
     * @return the corresponding response DTO
     */
    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {

    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {

    }


    private CategoryResponse toResponse(Category category) {

    }
}
