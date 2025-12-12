import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ApiService } from '../../../services/api.service';
import { AuthService, User, RegisterRequest } from '../../../services/auth.service';

@Component({
  selector: 'app-usuario-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './usuario-form.component.html'
})
export class UsuarioFormComponent implements OnInit {
  usuarioForm: FormGroup;
  isEditMode = false;
  usuarioId: number | null = null;
  loading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private apiService: ApiService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.usuarioForm = this.fb.group({
      nombre: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      rol: ['', Validators.required],
      telefono: [''],
      contraseña: [''],
      activo: [true]
    });
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.usuarioId = +params['id'];
        this.loadUsuario();
        this.usuarioForm.get('contraseña')?.clearValidators();
        this.usuarioForm.get('email')?.clearValidators();
        this.usuarioForm.get('rol')?.clearValidators();
      } else {
        this.usuarioForm.get('contraseña')?.setValidators([Validators.required, Validators.minLength(6)]);
      }
      this.usuarioForm.updateValueAndValidity();
    });
  }

  loadUsuario(): void {
    if (!this.usuarioId) return;
    this.loading = true;
    this.apiService.getUsuarioById(this.usuarioId).subscribe({
      next: (usuario) => {
        this.usuarioForm.patchValue({
          nombre: usuario.nombre,
          email: usuario.email,
          rol: usuario.rol,
          telefono: usuario.telefono || '',
          activo: usuario.activo
        });
        this.loading = false;
      },
      error: (err) => {
        console.error('Error al cargar usuario:', err);
        alert('Error al cargar el usuario');
        this.router.navigate(['/usuarios']);
      }
    });
  }

  guardarUsuario(): void {
    if (this.usuarioForm.invalid) return;

    this.loading = true;
    this.errorMessage = '';

    const usuarioData: any = {
      nombre: this.usuarioForm.value.nombre,
      telefono: this.usuarioForm.value.telefono || null
    };

    if (!this.isEditMode) {
      usuarioData.email = this.usuarioForm.value.email;
      usuarioData.rol = this.usuarioForm.value.rol;
      usuarioData.contraseña = this.usuarioForm.value.contraseña;
      
      // Usar endpoint de registro a través de AuthService
      const registerData: RegisterRequest = {
        name: usuarioData.nombre,
        email: usuarioData.email,
        password: usuarioData.contraseña,
        role: usuarioData.rol,
        phone: usuarioData.telefono || undefined
      };
      
      this.authService.register(registerData).subscribe({
        next: () => {
          this.router.navigate(['/usuarios']);
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Error al crear el usuario';
          this.loading = false;
        }
      });
    } else {
      usuarioData.activo = this.usuarioForm.value.activo;
      
      this.apiService.updateUsuario(this.usuarioId!, usuarioData).subscribe({
        next: () => {
          this.router.navigate(['/usuarios']);
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Error al actualizar el usuario';
          this.loading = false;
        }
      });
    }
  }

  cancelar(): void {
    this.router.navigate(['/usuarios']);
  }
}





