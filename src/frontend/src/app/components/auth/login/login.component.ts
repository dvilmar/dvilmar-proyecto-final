import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  loginForm: FormGroup;
  loading = false;
  error = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    // Asegurar que no haya tokens o usuarios residuales al entrar a la página de login
    // Esto es importante porque si el usuario viene de un logout, necesitamos limpiar todo
    if (this.authService.getToken()) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      this.authService.loadUserFromStorage();
    }
    
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required]]
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.loading = true;
      this.error = '';
      
      this.authService.login(this.loginForm.value).subscribe({
        next: (response) => {
          this.loading = false;
          // Después del login, siempre redirigir a la página principal
          this.router.navigate(['/']);
        },
        error: (err) => {
          this.loading = false;
          console.error('Error en login:', err);
          if (err.status === 401 || err.status === 403) {
            this.error = 'Credenciales incorrectas';
          } else {
            this.error = 'Error al iniciar sesión. Por favor, intenta de nuevo.';
          }
        }
      });
    }
  }
}






