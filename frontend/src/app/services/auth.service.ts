import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private apiUrl = '/v1/auth';
  
  currentUser = signal<string | null>(null);
  currentUserRole = signal<string | null>(null);

  constructor() {
    const token = this.getToken();
    if (token) {
      const username = this.decodeTokenUsername(token);
      this.currentUser.set(username);
      const role = this.decodeTokenRole(token);
      this.currentUserRole.set(role);
    }
  }

  login(nickname: string, password_user: string): Observable<{ token: string }> {
    return this.http.post<{ token: string }>(`${this.apiUrl}/login`, { nickname, password: password_user }).pipe(
      tap(response => {
        if (response && response.token) {
          localStorage.setItem('auth_token', response.token);
          const username = this.decodeTokenUsername(response.token);
          this.currentUser.set(username || nickname);
          const role = this.decodeTokenRole(response.token);
          this.currentUserRole.set(role);
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem('auth_token');
    this.currentUser.set(null);
    this.currentUserRole.set(null);
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('auth_token');
  }

  isAdmin(): boolean {
    const role = this.currentUserRole();
    return role === 'ROLE_ADMIN';
  }

  getToken(): string | null {
    return localStorage.getItem('auth_token');
  }

  getUserId(): number {
    const token = this.getToken();
    if (!token) return 1;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.id || payload.userId || 1;
    } catch {
      return 1;
    }
  }

  private decodeTokenUsername(token: string): string | null {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.sub || null;
    } catch {
      return null;
    }
  }

  private decodeTokenRole(token: string): string | null {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.rol || payload.roles || null;
    } catch {
      return null;
    }
  }
}
