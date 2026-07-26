package com.ejada.practice.service;

import com.ejada.practice.dto.request.CategoryRequest;
import com.ejada.practice.dto.response.CategoryResponse;

import java.util.List;

/**
 * Service contract for product category management.
 * <p>Provides read and write operations on the category catalogue.
 * The implementation is {@link com.ejada.practice.service.impl.CategoryServiceImpl}.</p>
 */
public interface CategoryService {

    /**
     * Returns all categories currently stored in the database.
     * @return an unordered list of {@link CategoryResponse} objects; never {@code null}
     */
    List<CategoryResponse> getAllCategories();

    /**
     * Creates a new category with the given name.
     * @param request the creation payload (category name)
     * @return a {@link CategoryResponse} representing the newly persisted category
     * @throws com.ejada.practice.exception.DuplicateResourceException if a category
     *         with the same name already exists
     */
    CategoryResponse createCategory(CategoryRequest request);
}
