import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProductService } from '../../../core/services/product.service';
import { CategoryService } from '../../../core/services/category.service';
import { OrderService } from '../../../core/services/order.service';
import { UserService } from '../../../core/services/user.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit {
  totalProducts: number | null = null;
  totalCategories: number | null = null;
  totalOrders: number | null = null;
  totalUsers: number | null = null;
  isLoading = true;

  constructor(
    private productService: ProductService,
    private categoryService: CategoryService,
    private orderService: OrderService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.productService.getAllProducts(undefined, 0, 1).subscribe({
      next: (result) => (this.totalProducts = result.totalElements),
      error: () => (this.totalProducts = null)
    });

    this.categoryService.getAllCategories().subscribe({
      next: (categories) => (this.totalCategories = categories.length),
      error: () => (this.totalCategories = null)
    });

    this.orderService.getAllOrders(undefined, undefined, 0, 1).subscribe({
      next: (result) => (this.totalOrders = result.totalElements),
      error: () => (this.totalOrders = null)
    });

    this.userService.getAllUsers(0, 1).subscribe({
      next: (result) => {
        this.totalUsers = result.totalElements;
        this.isLoading = false;
      },
      error: () => {
        this.totalUsers = null;
        this.isLoading = false;
      }
    });
  }
}
