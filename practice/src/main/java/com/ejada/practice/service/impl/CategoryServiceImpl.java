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

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        categoryRepository.findByName(request.getName()).ifPresent(category -> {
            throw new DuplicateResourceException("Category already exists: " + request.getName());
        });
        return toResponse(categoryRepository.save(Category.builder().name(request.getName()).build()));
    }

    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder().id(category.getId()).name(category.getName()).build();
    }
}
