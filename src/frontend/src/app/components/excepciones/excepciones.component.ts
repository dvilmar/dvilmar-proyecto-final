import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AuthService, User } from '../../services/auth.service';

@Component({
  selector: 'app-excepciones',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './excepciones.component.html'
})
export class ExcepcionesComponent implements OnInit {
  currentUser: User | null = null;
  excepciones: any[] = [];
  estilistas: any[] = [];
  excepcionForm: FormGroup;
  todoElDia = false;
  loading = false;
  errorMessage = '';
  minDate = new Date().toISOString().split('T')[0];

  constructor(
    private fb: FormBuilder,
    private apiService: ApiService,
    private authService: AuthService
  ) {
    this.excepcionForm = this.fb.group({
      date: ['', Validators.required],
      stylistId: [''],
      type: ['', Validators.required],
      startTime: [''],
      endTime: [''],
      reason: ['']
    });
  }

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.cargarExcepciones();
    this.cargarEstilistas();
  }

  cargarExcepciones(): void {
    this.loading = true;
    this.apiService.getExcepciones().subscribe({
      next: (data) => {
        this.excepciones = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error al cargar excepciones:', err);
        console.error('Error completo:', JSON.stringify(err, null, 2));
        this.errorMessage = err.error?.message || err.message || 'Error al cargar excepciones';
        this.loading = false;
      }
    });
  }

  cargarEstilistas(): void {
    this.apiService.getEstilistas().subscribe({
      next: (data) => {
        this.estilistas = data.filter((u: any) => u.rol === 'ESTILISTA');
      },
      error: (err) => {
        console.error('Error al cargar estilistas:', err);
        console.error('Error completo:', JSON.stringify(err, null, 2));
        this.errorMessage = err.error?.message || err.message || 'Error al cargar estilistas';
      }
    });
  }

  toggleTodoElDia(event: any): void {
    this.todoElDia = event.target.checked;
    if (this.todoElDia) {
      this.excepcionForm.patchValue({ startTime: null, endTime: null });
      this.excepcionForm.get('startTime')?.clearValidators();
      this.excepcionForm.get('endTime')?.clearValidators();
    } else {
      // No requerir horas por defecto
    }
    this.excepcionForm.get('startTime')?.updateValueAndValidity();
    this.excepcionForm.get('endTime')?.updateValueAndValidity();
  }

  crearExcepcion(): void {
    if (this.excepcionForm.invalid) return;

    this.loading = true;
    this.errorMessage = '';
    
    const formValue = this.excepcionForm.value;
    const excepcionData: any = {
      date: formValue.date,
      type: formValue.type,
      reason: formValue.reason || null
    };

    if (formValue.stylistId) {
      excepcionData.stylistId = formValue.stylistId;
    }

    if (!this.todoElDia && formValue.startTime && formValue.endTime) {
      excepcionData.startTime = formValue.startTime;
      excepcionData.endTime = formValue.endTime;
    }

    this.apiService.createExcepcion(excepcionData).subscribe({
      next: () => {
        this.excepcionForm.reset();
        this.todoElDia = false;
        this.cargarExcepciones();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error al crear la excepción';
        this.loading = false;
      }
    });
  }

  eliminarExcepcion(id: number): void {
    if (confirm('¿Estás seguro de que quieres eliminar esta excepción?')) {
      this.apiService.deleteExcepcion(id).subscribe({
        next: () => {
          this.cargarExcepciones();
        },
        error: (err) => {
          alert('Error al eliminar la excepción');
          console.error(err);
        }
      });
    }
  }

  formatDate(date: string): string {
    if (!date) return '';
    const d = new Date(date);
    return d.toLocaleDateString('es-ES');
  }
}





