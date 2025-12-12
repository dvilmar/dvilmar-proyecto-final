import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService, User } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  currentUser: User | null = null;
  citas: any[] = [];
  loading = false;

  constructor(
    private apiService: ApiService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.loadCitas();
  }

  loadCitas(): void {
    this.loading = true;
    
    // Si es estilista, filtrar por estilistaId
    // Si es cliente, filtrar por clienteId
    const filters: any = {};
    if (this.currentUser?.rol === 'ESTILISTA') {
      filters.estilistaId = this.currentUser.usuarioId;
    } else if (this.currentUser?.rol === 'CLIENTE') {
      filters.clienteId = this.currentUser.usuarioId;
    }
    
    this.apiService.getCitas(filters).subscribe({
      next: (data) => {
        this.citas = Array.isArray(data) ? data : (data.content || []);
        this.loading = false;
      },
      error: (err) => {
        console.error('Error al cargar citas:', err);
        this.loading = false;
      }
    });
  }

  cancelarCita(citaId: number): void {
    if (confirm('¿Estás seguro de que quieres cancelar esta cita?')) {
      this.apiService.updateCita(citaId, { estado: 'CANCELADA' }).subscribe({
        next: () => {
          this.loadCitas();
        },
        error: (err) => {
          console.error('Error al cancelar cita:', err);
          alert('Error al cancelar la cita');
        }
      });
    }
  }

  formatDate(date: string): string {
    if (!date) return '';
    // Si la fecha viene como string "YYYY-MM-DD", parsearla directamente sin conversión de zona horaria
    if (date.match(/^\d{4}-\d{2}-\d{2}$/)) {
      const parts = date.split('-');
      const d = new Date(parseInt(parts[0]), parseInt(parts[1]) - 1, parseInt(parts[2]));
      return d.toLocaleDateString('es-ES');
    }
    // Si viene como ISO string, parsear con cuidado
    const d = new Date(date + (date.includes('T') ? '' : 'T00:00:00'));
    // Usar métodos locales para evitar problemas de zona horaria
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${day}/${month}/${year}`;
  }
}






