import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../../../core/services/product.service';
import { CategoryService } from '../../../core/services/category.service';
import { ProductResponse } from '../../../core/models/product.models';
import { CategoryResponse } from '../../../core/models/category.models';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.css']
})
export class ProductListComponent implements OnInit {
  products: ProductResponse[] = [];
  categories: CategoryResponse[] = [];
  selectedCategoryId: number | null = null;

  isLoading = true;
  error: string | null = null;

  page = 0;
  pageSize = 12;
  totalPages = 0;

  constructor(
    private productService: ProductService,
    private categoryService: CategoryService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.categoryService.getAllCategories().subscribe({
      next: (categories) => (this.categories = categories),
      error: () => {
      }
    });

    this.route.queryParamMap.subscribe((params) => {
      const raw = params.get('categoryId');
      this.selectedCategoryId = raw ? Number(raw) : null;
      this.page = 0;
      this.loadProducts();
    });
  }

  loadProducts(): void {
    this.isLoading = true;
    this.error = null;

    const categoryId = this.selectedCategoryId ?? undefined;

    this.productService.getAllProducts(categoryId, this.page, this.pageSize).subscribe({
      next: (result) => {
        this.products = result.content;
        this.totalPages = result.totalPages;
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Failed to load products. Please try again later.';
        this.isLoading = false;
      }
    });
  }

  onCategoryChange(): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { categoryId: this.selectedCategoryId },
      queryParamsHandling: 'merge'
    });
  }

  goToPage(newPage: number): void {
    if (newPage < 0 || newPage >= this.totalPages) return;
    this.page = newPage;
    this.loadProducts();
  }
}
