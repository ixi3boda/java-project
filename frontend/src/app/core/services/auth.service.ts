import { HttpClient } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { LoginRequest, RegisterRequest, JwtResponse, UserResponse } from '../models/auth.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = '/api/auth';
  
  public currentUser = signal<string | null>(localStorage.getItem('access_token'));

  constructor(private http: HttpClient) {}

  login(request: LoginRequest): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(`${this.apiUrl}/login`, request).pipe(
      tap(response => {
        localStorage.setItem('access_token', response.accessToken);
        this.currentUser.set(response.accessToken);
      })
    );
  }

  register(request: RegisterRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${this.apiUrl}/register`, request);
  }

  logout(): void {
    localStorage.removeItem('access_token');
    this.currentUser.set(null);
  }

  isAuthenticated(): boolean {
    return !!this.currentUser();
  }


  isAdmin(): boolean {
    const token = this.currentUser();
    if (!token) return false;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const roles = payload.roles || payload.authorities || [];
      return roles.includes('ROLE_ADMIN') || roles.includes('ADMIN');
    } catch (e) {
      return false;
    }
  }
}
