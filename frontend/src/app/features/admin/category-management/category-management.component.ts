import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CategoryService } from '../../../core/services/category.service';
import { CategoryResponse } from '../../../core/models/category.models';

@Component({
  selector: 'app-category-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './category-management.component.html',
  styleUrl: './category-management.component.css'
})
export class CategoryManagementComponent implements OnInit {
  categories: CategoryResponse[] = [];
  isLoading = true;
  error: string | null = null;

  newCategoryName = '';
  isSaving = false;
  saveError: string | null = null;

  constructor(private categoryService: CategoryService) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.isLoading = true;
    this.error = null;

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

  createCategory(): void {
    const name = this.newCategoryName.trim();
    if (!name) return;

    this.isSaving = true;
    this.saveError = null;

    this.categoryService.createCategory({ name }).subscribe({
      next: () => {
        this.newCategoryName = '';
        this.isSaving = false;
        this.loadCategories();
      },
      error: (err) => {
        this.isSaving = false;
        this.saveError = err.error?.message || 'Failed to create category.';
      }
    });
  }
}
