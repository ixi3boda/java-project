package com.ejada.practice.service.impl;

import com.ejada.practice.dto.request.ProductRequest;
import com.ejada.practice.entity.Category;
import com.ejada.practice.entity.Product;
import com.ejada.practice.exception.ResourceNotFoundException;
import com.ejada.practice.repository.CategoryRepository;
import com.ejada.practice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {
    @Mock ProductRepository products; 
    @Mock CategoryRepository categories;
    @InjectMocks ProductServiceImpl service;

    @Test void createsProductWithResolvedCategories() {
        ProductRequest r = new ProductRequest(); 
        r.setName("Book"); 
        r.setPrice(BigDecimal.TEN); 
        r.setStockQuantity(4); 
        r.setCategoryIds(Set.of(2L));
        when(categories.findById(2L)).thenReturn(Optional.of(Category.builder().name("Books").build()));
        when(products.save(any())).thenAnswer(i -> i.getArgument(0));
        var result = service.createProduct(r);
        assertEquals("Book", result.getName()); 
        assertEquals(Set.of("Books"), result.getCategories());
    }
    @Test void missingProductProducesNotFound() {
        when(products.findById(9L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getProductById(9L));
    }
}
