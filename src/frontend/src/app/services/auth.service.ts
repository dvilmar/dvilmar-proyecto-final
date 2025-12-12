import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Router } from '@angular/router';

const API_URL = 'http://localhost:8080/api';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  role: string;
  phone?: string;
}

export interface AuthResponse {
  token: string;
  type?: string;
  tipo?: string;
  userId: number;
  usuarioId?: number; // Para compatibilidad
  name: string;
  nombre?: string; // Para compatibilidad
  email: string;
  role: string | any; // Puede venir como enum o string
  rol?: string; // Para compatibilidad
}

export interface User {
  usuarioId: number;
  nombre: string;
  email: string;
  rol: string;
  activo: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    this.loadUserFromStorage();
  }

  loadUserFromStorage(): void {
    const token = localStorage.getItem('token');
    const userStr = localStorage.getItem('user');
    if (token && userStr) {
      try {
        const user = JSON.parse(userStr);
        // Validar que el usuario tenga los campos necesarios
        if (user && user.usuarioId && user.rol && user.nombre) {
          this.currentUserSubject.next(user);
        } else {
          // Si el usuario es inválido, limpiar el storage
          this.clearStorage();
        }
      } catch (e) {
        // Si hay error al parsear, limpiar el storage
        this.clearStorage();
      }
    } else {
      // Si no hay token o usuario, asegurar que el estado esté limpio
      this.currentUserSubject.next(null);
    }
  }

  private clearStorage(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.currentUserSubject.next(null);
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    // Asegurar que no haya tokens residuales antes de hacer login
    // Esto previene problemas cuando se hace logout y luego login inmediatamente
    const oldToken = localStorage.getItem('token');
    if (oldToken) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      this.currentUserSubject.next(null);
    }
    
    return this.http.post<AuthResponse>(`${API_URL}/auth/login`, credentials, {
      headers: {
        'Content-Type': 'application/json'
        // No incluir Authorization header para login
      }
    })
      .pipe(
        tap(response => {
          localStorage.setItem('token', response.token);
          
          // Mapear los campos del backend (camelCase) al formato esperado por el frontend
          // El backend envía: userId, name, role
          // El frontend espera: usuarioId, nombre, rol
          const usuarioId = response.usuarioId || response.userId;
          const nombre = response.nombre || response.name;
          const roleValue = response.role || response.rol;
          // Asegurar que el rol sea un string (por si viene como enum del backend)
          const rol = typeof roleValue === 'string' ? roleValue : String(roleValue);
          
          const user: User = {
            usuarioId: usuarioId,
            nombre: nombre,
            email: response.email || '',
            rol: rol,
            activo: true
          };
          
          localStorage.setItem('user', JSON.stringify(user));
          this.currentUserSubject.next(user);
          console.log('Respuesta del backend:', response);
          console.log('Usuario guardado después del login:', user);
        })
      );
  }

  register(data: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API_URL}/auth/register`, data)
      .pipe(
        tap(response => {
          localStorage.setItem('token', response.token);
          
          // Mapear los campos del backend (camelCase) al formato esperado por el frontend
          const usuarioId = response.usuarioId || response.userId;
          const nombre = response.nombre || response.name;
          const roleValue = response.role || response.rol;
          const rol = typeof roleValue === 'string' ? roleValue : String(roleValue);
          
          const user: User = {
            usuarioId: usuarioId,
            nombre: nombre,
            email: response.email || '',
            rol: rol,
            activo: true
          };
          localStorage.setItem('user', JSON.stringify(user));
          this.currentUserSubject.next(user);
        })
      );
  }

  logout(): void {
    // Limpiar primero el estado local
    this.clearStorage();
    
    // Intentar hacer logout en el backend (sin headers de autenticación)
    // Pero no bloquear si falla, ya que el logout se maneja principalmente en el frontend
    this.http.post(`${API_URL}/auth/logout`, {}, {
      headers: {
        'Content-Type': 'application/json'
        // No incluir Authorization header
      }
    }).subscribe({
      next: () => {},
      error: () => {} // Ignorar errores en logout
    });
    
    // Navegar a la página principal
    this.router.navigate(['/']);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }
  
  getCurrentUserObservable(): Observable<User | null> {
    return this.currentUser$;
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }
  
  /**
   * Establece el usuario actual directamente (útil después de guardar en localStorage)
   */
  setCurrentUser(user: User | null): void {
    this.currentUserSubject.next(user);
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    const user = this.getCurrentUser();
    // Para estar autenticado, debe haber token Y un usuario válido
    return !!(token && user && user.usuarioId && user.rol);
  }

  hasRole(role: string): boolean {
    const user = this.getCurrentUser();
    return user?.rol === role;
  }
}






