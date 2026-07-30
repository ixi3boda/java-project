import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { CategoryService } from '../../../core/services/category.service';
import { CategoryResponse } from '../../../core/models/category.models';

@Component({
  selector: 'app-category-browse',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './category-browse.component.html',
  styleUrl: './category-browse.component.css'
})
export class CategoryBrowseComponent implements OnInit {
  categories: CategoryResponse[] = [];
  isLoading = true;
  error: string | null = null;

  constructor(
    private categoryService: CategoryService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.categoryService.getAllCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Failed to load categories.';
        this.isLoading = false;
      }
    });
  }

  browseCategory(categoryId: number): void {
    this.router.navigate(['/products'], { queryParams: { categoryId } });
  }
}
