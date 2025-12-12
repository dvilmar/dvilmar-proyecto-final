import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ApiService } from '../../../services/api.service';
import { AuthService, User } from '../../../services/auth.service';

@Component({
  selector: 'app-historial-citas',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './historial-citas.component.html',
  styleUrls: ['./historial-citas.component.css']
})
export class HistorialCitasComponent implements OnInit {
  currentUser: User | null = null;
  citasPasadas: any[] = [];
  citasPasadasOriginales: any[] = []; // Para aplicar filtros
  loading = false;
  errorMessage = '';
  
  // Filtros
  filtroFechaDesde = '';
  filtroFechaHasta = '';
  filtroEstilistaId = '';
  filtroEstado = '';
  estilistas: any[] = [];
  
  // Paginación
  usePagination = false;
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;
  pageData: any = null;

  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    public router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    
    if (!this.currentUser || this.currentUser.rol !== 'CLIENTE') {
      this.router.navigate(['/dashboard']);
      return;
    }
    
    this.loadEstilistas();
    this.loadHistorial();
  }

  loadEstilistas(): void {
    this.apiService.getEstilistas().subscribe({
      next: (data) => {
        this.estilistas = Array.isArray(data) ? data.filter((u: any) => u.rol === 'ESTILISTA') : [];
      },
      error: (err) => {
        console.error('Error al cargar estilistas:', err);
      }
    });
  }

  loadHistorial(): void {
    this.loading = true;
    this.errorMessage = '';
    
    const filters: any = {};
    // No podemos filtrar por múltiples estados en un solo parámetro, 
    // así que filtramos en el frontend
    
    // Filtrar citas pasadas (la fecha debe ser menor a hoy)
    // Este filtro se aplicará en el frontend si el backend no lo soporta
    
    this.apiService.getCitas(filters, this.currentPage, this.pageSize, this.usePagination).subscribe({
      next: (data) => {
        let citas: any[] = [];
        
        if (this.usePagination && data.content) {
          this.pageData = data;
          citas = data.content;
          this.totalPages = data.totalPages;
          this.totalElements = data.totalElements;
        } else {
          citas = Array.isArray(data) ? data : [];
          this.pageData = null;
        }
        
        // Filtrar solo citas pasadas (fecha < hoy)
        const hoy = new Date();
        hoy.setHours(0, 0, 0, 0);
        
        this.citasPasadasOriginales = citas.filter(cita => {
          const fechaCita = new Date(cita.date);
          fechaCita.setHours(0, 0, 0, 0);
          return fechaCita < hoy;
        }).sort((a, b) => {
          // Ordenar por fecha descendente (más recientes primero)
          const fechaA = new Date(a.date);
          const fechaB = new Date(b.date);
          return fechaB.getTime() - fechaA.getTime();
        });
        
        // Aplicar filtros adicionales
        this.aplicarFiltros();
        
        this.loading = false;
      },
      error: (err) => {
        console.error('Error al cargar historial de citas:', err);
        this.errorMessage = 'Error al cargar el historial de citas';
        this.loading = false;
      }
    });
  }

  aplicarFiltros(): void {
    let citasFiltradas = [...this.citasPasadasOriginales];
    
    // Filtrar por fecha desde
    if (this.filtroFechaDesde) {
      const fechaDesde = new Date(this.filtroFechaDesde);
      citasFiltradas = citasFiltradas.filter(cita => {
        const fechaCita = new Date(cita.date);
        fechaCita.setHours(0, 0, 0, 0);
        return fechaCita >= fechaDesde;
      });
    }
    
    // Filtrar por fecha hasta
    if (this.filtroFechaHasta) {
      const fechaHasta = new Date(this.filtroFechaHasta);
      citasFiltradas = citasFiltradas.filter(cita => {
        const fechaCita = new Date(cita.date);
        fechaCita.setHours(0, 0, 0, 0);
        return fechaCita <= fechaHasta;
      });
    }
    
    // Filtrar por estilista
    if (this.filtroEstilistaId) {
      citasFiltradas = citasFiltradas.filter(cita => 
        cita.stylistId === parseInt(this.filtroEstilistaId)
      );
    }
    
    // Filtrar por estado
    if (this.filtroEstado) {
      citasFiltradas = citasFiltradas.filter(cita => cita.status === this.filtroEstado);
    }
    
    this.citasPasadas = citasFiltradas;
  }

  limpiarFiltros(): void {
    this.filtroFechaDesde = '';
    this.filtroFechaHasta = '';
    this.filtroEstilistaId = '';
    this.filtroEstado = '';
    this.currentPage = 0;
    this.loadHistorial();
  }

  formatDate(date: string): string {
    if (!date) return '';
    const d = new Date(date);
    return d.toLocaleDateString('es-ES', { 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric' 
    });
  }

  formatTime(time: string): string {
    if (!time) return '';
    // Formato HH:mm
    return time.substring(0, 5);
  }

  getEstadoBadgeClass(status: string): string {
    switch (status) {
      case 'FINALIZADA':
        return 'badge bg-success';
      case 'CANCELADA':
        return 'badge bg-danger';
      default:
        return 'badge bg-secondary';
    }
  }

  getEstadoTexto(status: string): string {
    switch (status) {
      case 'FINALIZADA':
        return 'Finalizada';
      case 'CANCELADA':
        return 'Cancelada';
      default:
        return status;
    }
  }

  getEstilistaNombre(stylistId: number): string {
    const estilista = this.estilistas.find(e => e.usuarioId === stylistId);
    return estilista ? estilista.nombre : 'N/A';
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadHistorial();
  }

  togglePagination(): void {
    this.usePagination = !this.usePagination;
    this.currentPage = 0;
    this.loadHistorial();
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

  getTotalCitas(): number {
    return this.citasPasadasOriginales.length;
  }

  getCitasFinalizadas(): number {
    return this.citasPasadasOriginales.filter(c => c.status === 'FINALIZADA').length;
  }

  getCitasCanceladas(): number {
    return this.citasPasadasOriginales.filter(c => c.status === 'CANCELADA').length;
  }

  getTotalGastado(): number {
    return this.citasPasadasOriginales
      .filter(c => c.status === 'FINALIZADA')
      .reduce((sum, c) => sum + (c.totalPrice || 0), 0);
  }

  getTotalCitasFiltradas(): number {
    return this.citasPasadas.length;
  }

  getCitasFinalizadasFiltradas(): number {
    return this.citasPasadas.filter(c => c.status === 'FINALIZADA').length;
  }

  getCitasCanceladasFiltradas(): number {
    return this.citasPasadas.filter(c => c.status === 'CANCELADA').length;
  }

  getTotalGastadoFiltrado(): number {
    return this.citasPasadas
      .filter(c => c.status === 'FINALIZADA')
      .reduce((sum, c) => sum + (c.totalPrice || 0), 0);
  }
}




