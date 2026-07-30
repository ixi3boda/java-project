import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserService } from '../../../core/services/user.service';
import { UserResponse } from '../../../core/models/auth.models';

const AVAILABLE_ROLES = ['USER', 'ADMIN'];

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.css'
})
export class UserManagementComponent implements OnInit {
  readonly availableRoles = AVAILABLE_ROLES;

  users: UserResponse[] = [];
  isLoading = true;
  error: string | null = null;
  formError: string | null = null;
  isSaving = false;

  page = 0;
  pageSize = 20;
  totalPages = 0;

  isModalOpen = false;
  editingUserId: number | null = null;
  userForm: FormGroup;

  constructor(private userService: UserService, private fb: FormBuilder) {
    this.userForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: [''],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      roles: [[] as string[]]
    });
  }

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.isLoading = true;
    this.error = null;

    this.userService.getAllUsers(this.page, this.pageSize).subscribe({
      next: (result) => {
        this.users = result.content;
        this.totalPages = result.totalPages;
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Failed to load users.';
        this.isLoading = false;
      }
    });
  }

  goToPage(newPage: number): void {
    if (newPage < 0 || newPage >= this.totalPages) return;
    this.page = newPage;
    this.loadUsers();
  }

  openCreateModal(): void {
    this.editingUserId = null;
    this.formError = null;

    this.userForm.reset({
      username: '',
      email: '',
      password: '',
      firstName: '',
      lastName: '',
      roles: ['USER']
    });
    this.userForm.get('username')?.enable();
    this.userForm.get('password')?.setValidators([Validators.required, Validators.minLength(8)]);
    this.userForm.get('password')?.updateValueAndValidity();

    this.isModalOpen = true;
  }

  openEditModal(user: UserResponse): void {
    this.editingUserId = user.id;
    this.formError = null;

    this.userForm.reset({
      username: user.username,
      email: user.email,
      password: '',
      firstName: user.firstName,
      lastName: user.lastName,
      roles: [...user.roles]
    });
    this.userForm.get('username')?.disable();
    this.userForm.get('password')?.clearValidators();
    this.userForm.get('password')?.updateValueAndValidity();

    this.isModalOpen = true;
  }

  closeModal(): void {
    this.isModalOpen = false;
  }

  isRoleSelected(role: string): boolean {
    const roles: string[] = this.userForm.value.roles || [];
    return roles.includes(role);
  }

  toggleRole(role: string, checked: boolean): void {
    const roles: string[] = [...(this.userForm.value.roles || [])];
    const updated = checked ? [...roles, role] : roles.filter((r) => r !== role);
    const control = this.userForm.get('roles');
    control?.setValue(updated);
    control?.markAsTouched();
  }

  saveUser(): void {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      return;
    }

    this.isSaving = true;
    this.formError = null;

    if (this.editingUserId) {
      const { email, firstName, lastName, roles } = this.userForm.getRawValue();
      this.userService.updateUser(this.editingUserId, { email, firstName, lastName, roles }).subscribe({
        next: () => {
          this.isSaving = false;
          this.isModalOpen = false;
          this.loadUsers();
        },
        error: (err) => {
          this.isSaving = false;
          this.formError = err.error?.message || 'Failed to update user.';
        }
      });
    } else {
      const { username, email, password, firstName, lastName, roles } = this.userForm.getRawValue();
      this.userService.createUser({ username, email, password, firstName, lastName, roles }).subscribe({
        next: () => {
          this.isSaving = false;
          this.isModalOpen = false;
          this.loadUsers();
        },
        error: (err) => {
          this.isSaving = false;
          this.formError = err.error?.message || 'Failed to create user.';
        }
      });
    }
  }

  deleteUser(user: UserResponse): void {
    const confirmed = confirm(`Delete user "${user.username}"? This cannot be undone.`);
    if (!confirmed) return;

    this.userService.deleteUser(user.id).subscribe({
      next: () => this.loadUsers(),
      error: () => {
        this.error = 'Failed to delete user.';
      }
    });
  }
}
