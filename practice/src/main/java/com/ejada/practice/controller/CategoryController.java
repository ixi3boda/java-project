package com.ejada.practice.controller;

import com.ejada.practice.dto.request.CategoryRequest;
import com.ejada.practice.dto.response.CategoryResponse;
import com.ejada.practice.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing product categories.
 *
 * <p>Reading categories is a public operation (no authentication required).
 * Creating new categories is restricted to users with the {@code ADMIN} role.</p>
 *
 * <p>Base path: {@code /api/categories}</p>
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    /** Service that encapsulates category business logic. */
    private final CategoryService categoryService;

    /**
     * Retrieves all available product categories.
     * @return {@code 200 OK} containing the full list of categories
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    /**
     * Creates a new product category.
     * Returns {@code 409 Conflict} if a category with the same name already exists.</p>
     * @param request the category creation payload (name)
     * @return {@code 201 Created} containing the newly created category
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
    }
}
