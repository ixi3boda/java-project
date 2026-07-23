package com.ejada.practice.repository;

import com.ejada.practice.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RepositoryIntegrationTest {
    @Autowired private UserRepository users;
    @Autowired private RoleRepository roles;
    @Autowired private CategoryRepository categories;
    @Autowired private ProductRepository products;
    @Autowired private OrderRepository orders;

    @Test void userAndRoleRepositoryQueriesWorkAgainstDatabase() {
        Role role = roles.save(Role.builder().name("USER").build());
        users.save(User.builder().username("sam").email("sam@example.com").password("hash")
                .firstName("Sam").lastName("Smith").roles(Set.of(role)).build());

        assertTrue(users.findByUsername("sam").isPresent());
        assertTrue(users.existsByUsername("sam"));
        assertTrue(users.existsByEmail("sam@example.com"));
        assertEquals("USER", roles.findByName("USER").orElseThrow().getName());
    }

    @Test void categoryProductAndOrderRepositoryQueriesWorkAgainstDatabase() {
        Category category = categories.save(Category.builder().name("Books").build());
        Product product = products.save(Product.builder().name("Book").price(BigDecimal.TEN).stockQuantity(5)
                .categories(Set.of(category)).build());
        User user = users.save(User.builder().username("sam").email("sam@example.com").password("hash")
                .firstName("Sam").lastName("Smith").build());
        Order pending = orders.save(Order.builder().user(user).status(OrderStatus.PENDING).orderDate(LocalDateTime.now())
                .totalAmount(BigDecimal.TEN).build());
        orders.save(Order.builder().user(user).status(OrderStatus.SHIPPED).orderDate(LocalDateTime.now())
                .totalAmount(BigDecimal.TEN).build());
        var page = PageRequest.of(0, 10);

        assertEquals("Books", categories.findByName("Books").orElseThrow().getName());
        assertEquals(1, products.findByCategories_Id(category.getId(), page).getTotalElements());
        assertEquals(2, orders.findByUserId(user.getId(), page).getTotalElements());
        assertEquals(1, orders.findByStatus(OrderStatus.SHIPPED, page).getTotalElements());
        assertEquals(pending.getId(), orders.findByUserIdAndStatus(user.getId(), OrderStatus.PENDING, page).getContent().get(0).getId());
    }
}
