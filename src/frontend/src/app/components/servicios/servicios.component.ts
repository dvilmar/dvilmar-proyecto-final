import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AuthService, User } from '../../services/auth.service';

@Component({
  selector: 'app-servicios',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './servicios.component.html'
})
export class ServiciosComponent implements OnInit {
  currentUser: User | null = null;
  servicios: any[] = [];
  misServicios: any[] = [];
  servicioForm: FormGroup;
  mostrarFormulario = false;
  servicioEditando: any = null;
  mostrarAsociacion = false;
  serviciosDisponibles: any[] = [];
  serviciosSeleccionados: number[] = [];
  loading = false;
  loadingMisServicios = false;
  errorMessage = '';
  
  // Paginación
  usePagination = true; // Siempre activada por defecto
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;
  pageData: any = null;

  constructor(
    private fb: FormBuilder,
    private apiService: ApiService,
    private authService: AuthService
  ) {
    this.servicioForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      duration: ['', [Validators.required, Validators.min(1)]],
      unitPrice: ['', [Validators.required, Validators.min(0)]]
    });
  }

  get canManage(): boolean {
    return this.currentUser?.rol === 'ESTILISTA' || this.currentUser?.rol === 'ADMINISTRADOR';
  }

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.cargarServicios();
    if (this.currentUser?.rol === 'ESTILISTA') {
      this.cargarMisServicios();
    }
  }

  cargarServicios(): void {
    this.loading = true;
    this.apiService.getServicios(this.currentPage, this.pageSize, this.usePagination).subscribe({
      next: (data) => {
        if (this.usePagination && data.content) {
          // Respuesta paginada
          this.pageData = data;
          this.servicios = data.content;
          this.totalPages = data.totalPages;
          this.totalElements = data.totalElements;
        } else {
          // Respuesta lista sin paginar
          this.servicios = Array.isArray(data) ? data : [];
          this.pageData = null;
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error al cargar servicios:', err);
        console.error('Error completo:', JSON.stringify(err, null, 2));
        this.errorMessage = err.error?.message || err.message || 'Error al cargar servicios';
        this.loading = false;
      }
    });
  }

  editarServicio(servicio: any): void {
    this.servicioEditando = servicio;
    this.servicioForm.patchValue({
      name: servicio.name,
      description: servicio.description || '',
      duration: servicio.duration,
      unitPrice: servicio.unitPrice
    });
    this.mostrarFormulario = true;
  }

  guardarServicio(): void {
    if (this.servicioForm.invalid) return;

    this.loading = true;
    this.errorMessage = '';
    
    const servicioData = this.servicioForm.value;

    if (this.servicioEditando) {
      this.apiService.updateServicio(this.servicioEditando.serviceId, servicioData).subscribe({
        next: () => {
          this.cancelarFormulario();
          this.cargarServicios();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Error al actualizar el servicio';
          this.loading = false;
        }
      });
    } else {
      this.apiService.createServicio(servicioData).subscribe({
        next: () => {
          this.cancelarFormulario();
          this.cargarServicios();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Error al crear el servicio';
          this.loading = false;
        }
      });
    }
  }

  eliminarServicio(id: number): void {
    if (confirm('¿Estás seguro de que quieres eliminar este servicio?')) {
      this.apiService.deleteServicio(id).subscribe({
        next: () => {
          this.cargarServicios();
        },
        error: (err) => {
          alert('Error al eliminar el servicio');
          console.error(err);
        }
      });
    }
  }

  cancelarFormulario(): void {
    this.mostrarFormulario = false;
    this.servicioEditando = null;
    this.servicioForm.reset();
    this.errorMessage = '';
  }

  cargarMisServicios(): void {
    if (!this.currentUser) return;
    this.loadingMisServicios = true;
    this.apiService.getMisServicios().subscribe({
      next: (data) => {
        this.misServicios = data;
        this.serviciosSeleccionados = data.map((s: any) => s.serviceId);
        this.loadingMisServicios = false;
      },
      error: (err) => {
        console.error('Error al cargar mis servicios:', err);
        this.loadingMisServicios = false;
      }
    });
  }

  abrirAsociacion(): void {
    if (this.servicios.length === 0) {
      this.cargarServicios();
    }
    this.serviciosDisponibles = [...this.servicios];
    // Restaurar selección actual
    this.serviciosSeleccionados = this.misServicios.map((s: any) => s.serviceId);
    this.mostrarAsociacion = true;
  }

  toggleServicio(serviceId: number): void {
    const index = this.serviciosSeleccionados.indexOf(serviceId);
    if (index > -1) {
      this.serviciosSeleccionados.splice(index, 1);
    } else {
      this.serviciosSeleccionados.push(serviceId);
    }
  }

  isServicioSeleccionado(serviceId: number): boolean {
    return this.serviciosSeleccionados.includes(serviceId);
  }

  guardarAsociacion(): void {
    if (!this.currentUser) return;
    
    this.loading = true;
    this.apiService.asociarServicios(this.currentUser.usuarioId, this.serviciosSeleccionados).subscribe({
      next: () => {
        this.mostrarAsociacion = false;
        this.cargarMisServicios();
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error al asociar servicios';
        this.loading = false;
      }
    });
  }

  cancelarAsociacion(): void {
    this.mostrarAsociacion = false;
    this.errorMessage = '';
    this.cargarMisServicios(); // Restaurar selección anterior
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.cargarServicios();
  }

  togglePagination(): void {
    this.usePagination = !this.usePagination;
    this.currentPage = 0;
    this.cargarServicios();
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





