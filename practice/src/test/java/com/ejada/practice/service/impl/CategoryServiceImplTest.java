package com.ejada.practice.service.impl;

import com.ejada.practice.dto.request.CategoryRequest;
import com.ejada.practice.entity.Category;
import com.ejada.practice.exception.DuplicateResourceException;
import com.ejada.practice.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    @Mock CategoryRepository repository; 
    @InjectMocks CategoryServiceImpl service;
    @Test void listsAndCreatesCategories() {
        when(repository.findAll()).thenReturn(List.of(Category.builder().id(1L).name("Books").build()));
        assertEquals("Books", service.getAllCategories().get(0).getName());
        CategoryRequest request = new CategoryRequest(); 
        request.setName("Games");
        when(repository.findByName("Games")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertEquals("Games", service.createCategory(request).getName());
    }
    @Test void rejectsDuplicateCategory() {
        CategoryRequest request = new CategoryRequest(); 
        request.setName("Books");
        when(repository.findByName("Books")).thenReturn(Optional.of(new Category()));
        assertThrows(DuplicateResourceException.class, () -> service.createCategory(request));
    }
}
