package com.ejada.practice.service;

import com.ejada.practice.dto.request.CategoryRequest;
import com.ejada.practice.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllCategories();
    CategoryResponse createCategory(CategoryRequest request);
}
