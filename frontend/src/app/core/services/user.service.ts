import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Page } from '../models/product.models';
import { UserResponse } from '../models/auth.models';
import {
  AdminUserCreateRequest,
  AdminUserUpdateRequest,
  UserSelfUpdateRequest
} from '../models/user.models';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = '/api/users';

  constructor(private http: HttpClient) {}

  getMyProfile(): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.apiUrl}/me`);
  }

  updateMyProfile(request: UserSelfUpdateRequest): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${this.apiUrl}/me`, request);
  }

  verifyEmail(token: string): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.apiUrl}/verify-email`, { params: { token } });
  }

  getAllUsers(page: number = 0, size: number = 20): Observable<Page<UserResponse>> {
    return this.http.get<Page<UserResponse>>(this.apiUrl, {
      params: { page: page.toString(), size: size.toString() }
    });
  }

  getUserById(id: number): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.apiUrl}/${id}`);
  }

  createUser(request: AdminUserCreateRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(this.apiUrl, request);
  }

  updateUser(id: number, request: AdminUserUpdateRequest): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${this.apiUrl}/${id}`, request);
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
