import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserService } from '../../../core/services/user.service';
import { UserResponse } from '../../../core/models/auth.models';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
  profile: UserResponse | null = null;
  isLoading = true;
  error: string | null = null;

  isSaving = false;
  saveError: string | null = null;
  emailChangePending = false;

  profileForm: FormGroup;

  constructor(private userService: UserService, private fb: FormBuilder) {
    this.profileForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]]
    });
  }

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.isLoading = true;
    this.error = null;

    this.userService.getMyProfile().subscribe({
      next: (profile) => {
        this.profile = profile;
        this.profileForm.patchValue({
          firstName: profile.firstName,
          lastName: profile.lastName,
          email: profile.email
        });
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Failed to load your profile.';
        this.isLoading = false;
      }
    });
  }

  save(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    this.isSaving = true;
    this.saveError = null;
    this.emailChangePending = false;

    const { firstName, lastName, email } = this.profileForm.value;
    const emailChanged = email !== this.profile?.email;

    this.userService.updateMyProfile({ firstName, lastName, email }).subscribe({
      next: (updated) => {
        this.isSaving = false;
        this.profile = updated;
        this.emailChangePending = emailChanged && updated.email !== email;
      },
      error: (err) => {
        this.isSaving = false;
        this.saveError = err.error?.message || 'Failed to update profile.';
      }
    });
  }
}
