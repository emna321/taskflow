import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest, RoleName, User } from '../models';

const STORAGE_KEY = 'taskflow_auth';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private api = `${environment.apiUrl}/auth`;

  /** Utilisateur connecte (null si visiteur) */
  readonly currentUser = signal<AuthResponse | null>(this.load());
  readonly isLoggedIn = computed(() => this.currentUser() !== null);
  readonly isAdmin = computed(() => this.hasRole('ADMIN'));

  login(data: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.api}/login`, data).pipe(
      tap(res => {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(res));
        this.currentUser.set(res);
      })
    );
  }

  register(data: RegisterRequest): Observable<User> {
    return this.http.post<User>(`${this.api}/register`, data);
  }

  logout(): void {
    localStorage.removeItem(STORAGE_KEY);
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  get token(): string | null {
    return this.currentUser()?.token ?? null;
  }

  hasRole(role: RoleName): boolean {
    return this.currentUser()?.roles?.includes(role) ?? false;
  }

  /** Met a jour l'indicateur hasPhoto apres l'envoi d'une nouvelle photo de profil. */
  marquerPhoto(hasPhoto: boolean): void {
    const u = this.currentUser();
    if (!u) return;
    const maj = { ...u, hasPhoto };
    localStorage.setItem(STORAGE_KEY, JSON.stringify(maj));
    this.currentUser.set(maj);
  }

  private load(): AuthResponse | null {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) return null;
      const auth: AuthResponse = JSON.parse(raw);
      return this.isExpired(auth.token) ? null : auth;
    } catch {
      return null;
    }
  }

  /** Lit la date d'expiration (exp) dans le payload du JWT. */
  private isExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
      return !payload.exp || payload.exp * 1000 < Date.now();
    } catch {
      return true;
    }
  }
}
