package com.ejada.practice.repository;

import com.ejada.practice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByCategories_Id(Long categoryId, Pageable pageable);
}
