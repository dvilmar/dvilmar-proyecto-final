import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AuthService, User } from '../../services/auth.service';

@Component({
  selector: 'app-disponibilidad',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './disponibilidad.component.html'
})
export class DisponibilidadComponent implements OnInit {
  currentUser: User | null = null;
  disponibilidades: any[] = [];
  disponibilidadForm: FormGroup;
  disponibilidadEditando: any = null;
  loading = false;
  errorMessage = '';
  
  // Paginación
  usePagination = false;
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;
  pageData: any = null;

  diasNombre: { [key: string]: string } = {
    'MONDAY': 'Lunes',
    'TUESDAY': 'Martes',
    'WEDNESDAY': 'Miércoles',
    'THURSDAY': 'Jueves',
    'FRIDAY': 'Viernes',
    'SATURDAY': 'Sábado',
    'SUNDAY': 'Domingo'
  };

  constructor(
    private fb: FormBuilder,
    private apiService: ApiService,
    private authService: AuthService
  ) {
    this.disponibilidadForm = this.fb.group({
      dayOfWeek: ['', Validators.required],
      startTime: ['', Validators.required],
      endTime: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.cargarDisponibilidades();
  }

  cargarDisponibilidades(): void {
    this.loading = true;
    const estilistaId = this.currentUser?.rol === 'ESTILISTA' ? this.currentUser.usuarioId : undefined;
    this.apiService.getDisponibilidades(estilistaId, this.currentPage, this.pageSize, this.usePagination).subscribe({
      next: (data) => {
        if (this.usePagination && data.content) {
          // Respuesta paginada
          this.pageData = data;
          this.disponibilidades = data.content;
          this.totalPages = data.totalPages;
          this.totalElements = data.totalElements;
        } else {
          // Respuesta lista sin paginar
          this.disponibilidades = Array.isArray(data) ? data : [];
          this.pageData = null;
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error al cargar disponibilidades:', err);
        this.loading = false;
      }
    });
  }

  crearDisponibilidad(): void {
    if (this.disponibilidadForm.invalid) return;

    this.loading = true;
    this.errorMessage = '';
    
    const formValue = this.disponibilidadForm.value;
    const disponibilidadData = {
      stylistId: this.currentUser?.usuarioId,
      dayOfWeek: formValue.dayOfWeek,
      startTime: formValue.startTime,
      endTime: formValue.endTime
    };

    if (this.disponibilidadEditando) {
      // Modo edición
      this.apiService.updateDisponibilidad(this.disponibilidadEditando.availabilityId, disponibilidadData).subscribe({
        next: () => {
          this.cancelarEdicion();
          this.cargarDisponibilidades();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Error al actualizar la disponibilidad';
          this.loading = false;
        }
      });
    } else {
      // Modo creación
      this.apiService.createDisponibilidad(disponibilidadData).subscribe({
        next: () => {
          this.disponibilidadForm.reset();
          this.cargarDisponibilidades();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Error al crear la disponibilidad';
          this.loading = false;
        }
      });
    }
  }

  editarDisponibilidad(disponibilidad: any): void {
    this.disponibilidadEditando = disponibilidad;
    this.disponibilidadForm.patchValue({
      dayOfWeek: disponibilidad.dayOfWeek,
      startTime: disponibilidad.startTime,
      endTime: disponibilidad.endTime
    });
    // Scroll al formulario
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  cancelarEdicion(): void {
    this.disponibilidadEditando = null;
    this.disponibilidadForm.reset();
    this.errorMessage = '';
  }

  eliminarDisponibilidad(id: number): void {
    if (confirm('¿Estás seguro de que quieres eliminar esta disponibilidad?')) {
      this.apiService.deleteDisponibilidad(id).subscribe({
        next: () => {
          this.cargarDisponibilidades();
        },
        error: (err) => {
          alert('Error al eliminar la disponibilidad');
          console.error(err);
        }
      });
    }
  }

  getDiaNombre(dayOfWeek: string): string {
    return this.diasNombre[dayOfWeek] || dayOfWeek;
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.cargarDisponibilidades();
  }

  togglePagination(): void {
    this.usePagination = !this.usePagination;
    this.currentPage = 0;
    this.cargarDisponibilidades();
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    const maxPages = 5;
    let start = Math.max(0, this.currentPage - Math.floor(maxPages / 2));
    let end = Math.min(this.totalPages, start + maxPages);
    if (end - start < maxPages) {
      start = Math.max(0, end - maxPages);
    }
    for (let i = start; i < end; i++) {
      pages.push(i);
    }
    return pages;
  }

  get Math() {
    return Math;
  }
}





