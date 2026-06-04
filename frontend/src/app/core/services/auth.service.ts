import { Injectable, inject } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { ApiService } from './api.service';
import { User } from '../models/models';

export interface LoginRequest { email: string; password: string; }
export interface RegisterRequest { firstName: string; lastName: string; email: string; password: string; organizationName?: string; }
export interface AuthResponse { accessToken: string; refreshToken: string; expiresIn: number; user: User; }
export interface RefreshRequest { refreshToken: string; }

const TOKEN_KEY   = 'ss_access_token';
const REFRESH_KEY = 'ss_refresh_token';
const USER_KEY    = 'ss_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = inject(ApiService);

  login(req: LoginRequest): Observable<AuthResponse> {
    return this.api.post<AuthResponse>('/api/v1/auth/login', req).pipe(
      tap(res => this.storeTokens(res))
    );
  }

  register(req: RegisterRequest): Observable<AuthResponse> {
    return this.api.post<AuthResponse>('/api/v1/auth/register', req).pipe(
      tap(res => this.storeTokens(res))
    );
  }

  refresh(): Observable<AuthResponse> {
    const refreshToken = this.getRefreshToken();
    return this.api.post<AuthResponse>('/api/v1/auth/refresh', { refreshToken }).pipe(
      tap(res => this.storeTokens(res))
    );
  }

  logout(): Observable<any> {
    return this.api.post('/api/v1/auth/logout', {}).pipe(
      tap(() => this.clearTokens())
    );
  }

  getAccessToken(): string | null  { return localStorage.getItem(TOKEN_KEY); }
  getRefreshToken(): string | null { return localStorage.getItem(REFRESH_KEY); }
  getStoredUser(): User | null {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  }
  isLoggedIn(): boolean { return !!this.getAccessToken(); }

  private storeTokens(res: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY,   res.accessToken);
    localStorage.setItem(REFRESH_KEY, res.refreshToken);
    localStorage.setItem(USER_KEY,    JSON.stringify(res.user));
  }

  clearTokens(): void {
    [TOKEN_KEY, REFRESH_KEY, USER_KEY].forEach(k => localStorage.removeItem(k));
  }
}
