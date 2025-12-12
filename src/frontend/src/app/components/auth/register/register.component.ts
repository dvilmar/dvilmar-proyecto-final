import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './register.component.html'
})
export class RegisterComponent {
  registerForm: FormGroup;
  loading = false;
  error = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      name: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      role: ['', [Validators.required]],
      phone: ['']
    });
  }

  onSubmit(): void {
    if (this.registerForm.valid) {
      this.loading = true;
      this.error = '';
      
      // Mapear los valores del formulario a lo que espera el backend
      const registerData = {
        name: this.registerForm.value.name,
        email: this.registerForm.value.email,
        password: this.registerForm.value.password,
        role: this.registerForm.value.role,
        phone: this.registerForm.value.role === 'CLIENTE' ? this.registerForm.value.phone : undefined
      };
      
      this.authService.register(registerData).subscribe({
        next: (response) => {
          this.loading = false;
          const user = this.authService.getCurrentUser();
          if (user?.rol === 'ADMINISTRADOR') {
            this.router.navigate(['/admin']);
          } else {
            this.router.navigate(['/dashboard']);
          }
        },
        error: (err) => {
          this.loading = false;
          this.error = 'Error al registrar. El email puede estar en uso.';
        }
      });
    }
  }
}






