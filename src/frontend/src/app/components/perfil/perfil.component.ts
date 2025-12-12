import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AuthService, User } from '../../services/auth.service';

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './perfil.component.html'
})
export class PerfilComponent implements OnInit {
  currentUser: User | null = null;
  perfilForm: FormGroup;
  passwordForm: FormGroup;
  loading = false;
  saving = false;
  savingPassword = false;
  errorMessage = '';
  successMessage = '';
  passwordErrorMessage = '';
  editandoPerfil = false;
  nombreOriginal = '';

  constructor(
    private fb: FormBuilder,
    private apiService: ApiService,
    private authService: AuthService
  ) {
    this.perfilForm = this.fb.group({
      nombre: ['', Validators.required]
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    // Inicializar el formulario con el nombre del usuario actual si está disponible
    if (this.currentUser?.nombre) {
      this.nombreOriginal = this.currentUser.nombre;
      this.perfilForm.patchValue({
        nombre: this.currentUser.nombre
      });
      this.perfilForm.get('nombre')?.disable();
    }
    this.cargarPerfil();
  }

  cargarPerfil(): void {
    // Si no hay usuario autenticado, no intentar cargar
    if (!this.authService.isAuthenticated() || !this.currentUser) {
      this.loading = false;
      return;
    }
    
    this.loading = true;
    
    // Cargar desde el servidor para obtener datos actualizados
    this.apiService.getCurrentUser().subscribe({
      next: (data) => {
        // Actualizar currentUser con los datos del servidor
        if (this.currentUser) {
          this.currentUser.nombre = data.nombre || this.currentUser.nombre;
          this.currentUser.email = data.email || this.currentUser.email;
          this.currentUser.rol = data.rol || this.currentUser.rol;
        } else {
          this.currentUser = {
            usuarioId: data.usuarioId,
            nombre: data.nombre,
            email: data.email,
            rol: data.rol
          } as User;
        }
        
        this.nombreOriginal = data.nombre || this.currentUser?.nombre || '';
        if (this.perfilForm) {
          this.perfilForm.patchValue({
            nombre: this.nombreOriginal
          });
          this.perfilForm.get('nombre')?.disable();
        }
        this.editandoPerfil = false;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error al cargar perfil:', err);
        // Si falla, usar el valor del usuario actual si está disponible
        if (this.currentUser?.nombre && this.perfilForm) {
          this.nombreOriginal = this.currentUser.nombre;
          this.perfilForm.patchValue({
            nombre: this.currentUser.nombre
          });
          this.perfilForm.get('nombre')?.disable();
        }
        this.loading = false;
      }
    });
  }

  editarPerfil(): void {
    this.editandoPerfil = true;
    this.perfilForm.get('nombre')?.enable();
    this.errorMessage = '';
    this.successMessage = '';
  }

  cancelarEdicion(): void {
    this.editandoPerfil = false;
    this.perfilForm.patchValue({
      nombre: this.nombreOriginal
    });
    this.perfilForm.get('nombre')?.disable();
    this.errorMessage = '';
    this.successMessage = '';
  }

  actualizarPerfil(): void {
    if (this.perfilForm.invalid) return;

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    const updateData = {
      nombre: this.perfilForm.value.nombre
    };

    this.apiService.updateProfile(updateData).subscribe({
      next: (data) => {
        this.nombreOriginal = data.nombre;
        this.successMessage = 'Perfil actualizado correctamente';
        this.authService.currentUser$.subscribe(user => {
          if (user) {
            user.nombre = data.nombre;
          }
        });
        this.editandoPerfil = false;
        this.perfilForm.get('nombre')?.disable();
        this.saving = false;
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error al actualizar el perfil';
        this.saving = false;
      }
    });
  }

  cambiarPassword(): void {
    if (this.passwordForm.invalid) return;

    this.savingPassword = true;
    this.passwordErrorMessage = '';

    const updateData = {
      contraseñaActual: this.passwordForm.value.currentPassword,
      contraseña: this.passwordForm.value.password
    };

    this.apiService.updateProfile(updateData).subscribe({
      next: () => {
        alert('Contraseña actualizada correctamente');
        this.passwordForm.reset();
        this.savingPassword = false;
      },
      error: (err) => {
        this.passwordErrorMessage = err.error?.message || 'Error al cambiar la contraseña';
        this.savingPassword = false;
      }
    });
  }
}





