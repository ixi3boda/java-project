import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';
import { ProductService } from '../../../core/services/product.service';
import { CategoryService } from '../../../core/services/category.service';
import { ProductResponse } from '../../../core/models/product.models';
import { CategoryResponse } from '../../../core/models/category.models';

function requireAtLeastOne(control: AbstractControl): ValidationErrors | null {
  const value = control.value as number[] | null;
  return value && value.length > 0 ? null : { required: true };
}

@Component({
  selector: 'app-product-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './product-management.component.html',
  styleUrl: './product-management.component.css'
})
export class ProductManagementComponent implements OnInit {
  products: ProductResponse[] = [];
  categories: CategoryResponse[] = [];

  isLoading = true;
  error: string | null = null;
  formError: string | null = null;
  isSaving = false;

  // Modal state
  isModalOpen = false;
  editingProductId: number | null = null;
  productForm: FormGroup;

  constructor(
    private productService: ProductService,
    private categoryService: CategoryService,
    private fb: FormBuilder
  ) {
    this.productForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      price: [0, [Validators.required, Validators.min(0.01)]],
      stockQuantity: [0, [Validators.required, Validators.min(0)]],
      categoryIds: [[] as number[], requireAtLeastOne]
    });
  }

  ngOnInit(): void {
    this.loadCategories();
    this.loadProducts();
  }

  loadProducts(): void {
    this.isLoading = true;
    this.error = null;

    // Admin view: pull a large page so the whole catalogue shows in one table.
    this.productService.getAllProducts(undefined, 0, 200).subscribe({
      next: (result) => {
        this.products = result.content;
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Failed to load products.';
        this.isLoading = false;
      }
    });
  }

  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe({
      next: (categories) => (this.categories = categories),
      error: () => {
        this.error = 'Failed to load categories. Add some from the Categories page first.';
      }
    });
  }

  openCreateModal(): void {
    if (this.categories.length === 0) {
      this.formError = null;
      this.error = 'You need at least one category before adding a product. Create one on the Categories page.';
      return;
    }

    this.editingProductId = null;
    this.formError = null;
    this.productForm.reset({
      name: '',
      description: '',
      price: 0,
      stockQuantity: 0,
      categoryIds: []
    });
    this.isModalOpen = true;
  }

  openEditModal(product: ProductResponse): void {
    this.editingProductId = product.id;
    this.formError = null;

    const categoryIds = this.categories
      .filter((c) => product.categories.includes(c.name))
      .map((c) => c.id);

    this.productForm.reset({
      name: product.name,
      description: product.description,
      price: product.price,
      stockQuantity: product.stockQuantity,
      categoryIds
    });
    this.isModalOpen = true;
  }

  closeModal(): void {
    this.isModalOpen = false;
  }

  isCategorySelected(categoryId: number): boolean {
    const ids: number[] = this.productForm.value.categoryIds || [];
    return ids.includes(categoryId);
  }

  toggleCategory(categoryId: number, checked: boolean): void {
    const ids: number[] = [...(this.productForm.value.categoryIds || [])];
    const updated = checked ? [...ids, categoryId] : ids.filter((id) => id !== categoryId);
    const control = this.productForm.get('categoryIds');
    control?.setValue(updated);
    control?.markAsTouched();
  }

  saveProduct(): void {
    if (this.productForm.invalid) {
      this.productForm.markAllAsTouched();
      return;
    }

    this.isSaving = true;
    this.formError = null;
    const payload = this.productForm.value;

    const request$ = this.editingProductId
      ? this.productService.updateProduct(this.editingProductId, payload)
      : this.productService.createProduct(payload);

    request$.subscribe({
      next: () => {
        this.isSaving = false;
        this.isModalOpen = false;
        this.loadProducts();
      },
      error: (err) => {
        this.isSaving = false;
        this.formError = err.error?.message || 'Failed to save product.';
      }
    });
  }

  deleteProduct(product: ProductResponse): void {
    const confirmed = confirm(`Delete "${product.name}"? This cannot be undone.`);
    if (!confirmed) return;

    this.productService.deleteProduct(product.id).subscribe({
      next: () => this.loadProducts(),
      error: () => {
        this.error = 'Failed to delete product.';
      }
    });
  }
}
