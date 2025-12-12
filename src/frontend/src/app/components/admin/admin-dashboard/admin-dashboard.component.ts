import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ApiService } from '../../../services/api.service';
import { AuthService, User } from '../../../services/auth.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './admin-dashboard.component.html'
})
export class AdminDashboardComponent implements OnInit {
  currentUser: User | null = null;
  citas: any[] = [];
  servicios: any[] = [];
  loading = false;
  loadingServicios = false;
  mostrarFormularioServicio = false;
  nuevoServicio: any = {
    nombre: '',
    descripcion: '',
    duracion: 0,
    precioUnitario: 0
  };

  get totalCitas(): number {
    return this.citas.length;
  }

  get citasConfirmadas(): number {
    return this.citas.filter(c => c.estado === 'CONFIRMADA').length;
  }

  get totalServicios(): number {
    return this.servicios.length;
  }

  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.loadCitas();
    this.loadServicios();
  }

  loadCitas(): void {
    this.loading = true;
    this.apiService.getCitas().subscribe({
      next: (data) => {
        this.citas = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error al cargar citas:', err);
        this.loading = false;
      }
    });
  }

  loadServicios(): void {
    this.loadingServicios = true;
    this.apiService.getServicios().subscribe({
      next: (data) => {
        this.servicios = data;
        this.loadingServicios = false;
      },
      error: (err) => {
        console.error('Error al cargar servicios:', err);
        this.loadingServicios = false;
      }
    });
  }

  editarCita(cita: any): void {
    this.router.navigate(['/citas', cita.citaId, 'editar']);
  }

  eliminarCita(citaId: number): void {
    if (confirm('¿Estás seguro de que quieres eliminar esta cita?')) {
      this.apiService.deleteCita(citaId).subscribe({
        next: () => {
          this.loadCitas();
        },
        error: (err) => {
          console.error('Error al eliminar cita:', err);
          alert('Error al eliminar la cita');
        }
      });
    }
  }

  crearServicio(): void {
    if (!this.nuevoServicio.nombre || !this.nuevoServicio.duracion || !this.nuevoServicio.precioUnitario) {
      alert('Por favor completa todos los campos obligatorios');
      return;
    }

    this.apiService.createServicio(this.nuevoServicio).subscribe({
      next: () => {
        this.mostrarFormularioServicio = false;
        this.nuevoServicio = { nombre: '', descripcion: '', duracion: 0, precioUnitario: 0 };
        this.loadServicios();
      },
      error: (err) => {
        console.error('Error al crear servicio:', err);
        alert('Error al crear el servicio');
      }
    });
  }

  eliminarServicio(servicioId: number): void {
    if (confirm('¿Estás seguro de que quieres eliminar este servicio?')) {
      this.apiService.deleteServicio(servicioId).subscribe({
        next: () => {
          this.loadServicios();
        },
        error: (err) => {
          console.error('Error al eliminar servicio:', err);
          alert('Error al eliminar el servicio');
        }
      });
    }
  }
}

