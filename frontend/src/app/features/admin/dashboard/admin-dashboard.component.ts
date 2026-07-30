import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProductService } from '../../../core/services/product.service';
import { CategoryService } from '../../../core/services/category.service';

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
  isLoading = true;

  constructor(
    private productService: ProductService,
    private categoryService: CategoryService
  ) {}

  ngOnInit(): void {
    this.productService.getAllProducts(undefined, 0, 1).subscribe({
      next: (result) => (this.totalProducts = result.totalElements),
      error: () => (this.totalProducts = null)
    });

    this.categoryService.getAllCategories().subscribe({
      next: (categories) => {
        this.totalCategories = categories.length;
        this.isLoading = false;
      },
      error: () => {
        this.totalCategories = null;
        this.isLoading = false;
      }
    });
  }
}
