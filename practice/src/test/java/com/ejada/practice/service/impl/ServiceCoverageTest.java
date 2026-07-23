package com.ejada.practice.service.impl;

import com.ejada.practice.dto.request.*;
import com.ejada.practice.entity.*;
import com.ejada.practice.exception.*;
import com.ejada.practice.repository.*;
import com.ejada.practice.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceCoverageTest {
    @Mock private UserRepository users;
    @Mock private RoleRepository roles;
    @Mock private CategoryRepository categories;
    @Mock private ProductRepository products;
    @Mock private OrderRepository orders;
    @Mock private PasswordEncoder encoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;
    @InjectMocks private AuthServiceImpl authService;
    @InjectMocks private ProductServiceImpl productService;
    @InjectMocks private UserServiceImpl userService;
    @InjectMocks private OrderServiceImpl orderService;

    @Test void authRegistrationCoversEmailAndMissingRoleFailures() {
        RegisterRequest request = registerRequest();
        when(users.existsByEmail("sam@example.com")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> authService.register(request));

        reset(users);
        when(roles.findByName("USER")).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> authService.register(request));
    }

    @Test void loginRejectsMissingAuthenticatedUser() {
        LoginRequest request = new LoginRequest(); request.setUsername("sam"); request.setPassword("password");
        when(users.findByUsername("sam")).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> authService.login(request));
        verify(authenticationManager).authenticate(any());
    }

    @Test void productListingCoversBothRepositoryQueries() {
        var pageable = PageRequest.of(0, 10);
        Product product = product();
        when(products.findAll(pageable)).thenReturn(new PageImpl<>(List.of(product)));
        when(products.findByCategories_Id(2L, pageable)).thenReturn(new PageImpl<>(List.of(product)));
        assertEquals(1, productService.getAllProducts(null, pageable).getTotalElements());
        assertEquals(1, productService.getAllProducts(2L, pageable).getTotalElements());
    }

    @Test void productUpdateDeleteAndMissingCategoryAreHandled() {
        ProductRequest request = productRequest();
        Product product = product();
        when(products.findById(1L)).thenReturn(Optional.of(product));
        when(categories.findById(2L)).thenReturn(Optional.of(Category.builder().name("Books").build()));
        when(products.save(any())).thenAnswer(call -> call.getArgument(0));
        assertEquals("New book", productService.updateProduct(1L, request).getName());
        productService.deleteProduct(1L);
        verify(products).delete(product);

        when(categories.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.createProduct(request));
    }

    @Test void userReadAndUpdatePathsAreCovered() {
        User user = user();
        when(users.findByUsername("sam")).thenReturn(Optional.of(user));
        var pageable = PageRequest.of(0, 10);
        when(users.findAll(pageable)).thenReturn(new PageImpl<>(List.of(user)));
        when(users.findById(1L)).thenReturn(Optional.of(user));
        when(users.save(any())).thenAnswer(call -> call.getArgument(0));
        var update = new UserSelfUpdateRequest(); update.setFirstName("Samuel"); update.setLastName("Smith");
        assertEquals("sam", userService.getOwnProfile("sam").getUsername());
        assertEquals("Samuel", userService.updateOwnProfile("sam", update).getFirstName());
        assertEquals(1, userService.getAllUsers(pageable).getTotalElements());
        assertEquals("sam", userService.getUserById(1L).getUsername());
        userService.deleteUser(1L);
        verify(users).delete(user);
    }

    @Test void userAdminFailuresAndRoleUpdateAreCovered() {
        AdminUserCreateRequest create = new AdminUserCreateRequest(); create.setUsername("sam"); create.setEmail("sam@example.com");
        when(users.existsByUsername("sam")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> userService.createUser(create));
        reset(users);
        when(users.existsByEmail("sam@example.com")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> userService.createUser(create));

        User user = user(); AdminUserUpdateRequest update = new AdminUserUpdateRequest(); update.setRoles(Set.of("ADMIN"));
        when(users.findById(1L)).thenReturn(Optional.of(user));
        when(roles.findByName("ADMIN")).thenReturn(Optional.of(Role.builder().name("ADMIN").build()));
        when(users.save(any())).thenAnswer(call -> call.getArgument(0));
        assertEquals(Set.of("ADMIN"), userService.updateUser(1L, update).getRoles());
    }

    @Test void orderQueriesCoverAllFilters() {
        var pageable = PageRequest.of(0, 10); Order order = order(user());
        when(orders.findByUserIdAndStatus(1L, OrderStatus.PENDING, pageable)).thenReturn(new PageImpl<>(List.of(order)));
        when(orders.findByStatus(OrderStatus.PENDING, pageable)).thenReturn(new PageImpl<>(List.of(order)));
        when(orders.findByUserId(1L, pageable)).thenReturn(new PageImpl<>(List.of(order)));
        when(orders.findAll(pageable)).thenReturn(new PageImpl<>(List.of(order)));
        assertEquals(1, orderService.getAllOrders(OrderStatus.PENDING, 1L, pageable).getTotalElements());
        assertEquals(1, orderService.getAllOrders(OrderStatus.PENDING, null, pageable).getTotalElements());
        assertEquals(1, orderService.getAllOrders(null, 1L, pageable).getTotalElements());
        assertEquals(1, orderService.getAllOrders(null, null, pageable).getTotalElements());
    }

    @Test void orderOwnershipAndMissingResourcesAreHandled() {
        Order order = order(user()); when(orders.findById(1L)).thenReturn(Optional.of(order));
        assertEquals(1L, orderService.getOrderById(1L, "sam", false).getId());
        assertThrows(AccessDeniedException.class, () -> orderService.getOrderById(1L, "other", false));
        reset(orders); when(orders.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(1L, "sam", true));
        assertThrows(ResourceNotFoundException.class, () -> orderService.updateStatus(1L, new OrderStatusUpdateRequest()));
    }

    @Test void orderPlacementAndLookupMissingUserOrProductAreHandled() {
        OrderRequest request = new OrderRequest(); request.setItems(List.of(itemRequest(2L)));
        when(users.findByUsername("sam")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> orderService.placeOrder("sam", request));
        when(users.findByUsername("sam")).thenReturn(Optional.of(user())); when(products.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> orderService.placeOrder("sam", request));
        when(users.findByUsername("missing")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> orderService.getMyOrders("missing", PageRequest.of(0, 10)));
    }

    private RegisterRequest registerRequest() { RegisterRequest r = new RegisterRequest(); r.setUsername("sam"); r.setEmail("sam@example.com"); r.setPassword("password"); r.setFirstName("Sam"); r.setLastName("Smith"); return r; }
    private ProductRequest productRequest() { ProductRequest r = new ProductRequest(); r.setName("New book"); r.setPrice(BigDecimal.TEN); r.setStockQuantity(2); r.setCategoryIds(Set.of(2L)); return r; }
    private OrderItemRequest itemRequest(Long id) { OrderItemRequest r = new OrderItemRequest(); r.setProductId(id); r.setQuantity(1); return r; }
    private User user() { return User.builder().id(1L).username("sam").email("sam@example.com").firstName("Sam").lastName("Smith").roles(Set.of(Role.builder().name("USER").build())).build(); }
    private Product product() { return Product.builder().id(3L).name("Book").price(BigDecimal.TEN).stockQuantity(3).categories(Set.of(Category.builder().name("Books").build())).build(); }
    private Order order(User user) { Product product = product(); return Order.builder().id(1L).user(user).status(OrderStatus.PENDING).orderDate(LocalDateTime.now()).totalAmount(BigDecimal.TEN).items(List.of(OrderItem.builder().product(product).quantity(1).priceAtPurchase(BigDecimal.TEN).build())).build(); }
}
