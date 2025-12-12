import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule, NavigationEnd } from '@angular/router';
import { ApiService } from '../../../services/api.service';
import { AuthService, User } from '../../../services/auth.service';
import { Subscription, filter } from 'rxjs';

@Component({
  selector: 'app-citas-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './citas-list.component.html'
})
export class CitasListComponent implements OnInit, OnDestroy {
  currentUser: User | null = null;
  citas: any[] = [];
  citasFiltradas: any[] = [];
  estilistas: any[] = [];
  servicios: any[] = [];
  loading = false;
  filtroFecha = '';
  filtroEstilistaId = '';
  filtroEstado = '';
  filtroNombreCliente = '';
  filtroNombreEstilista = '';
  filtroNombreServicio = '';
  
  // Filtros favoritos
  filtrosFavoritos: any[] = [];
  nombreFiltroFavorito = '';
  
  // Paginación
  usePagination = false;
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;
  pageData: any = null;
  
  private routerSubscription?: Subscription;

  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initializeComponent();
    
    // Suscribirse a eventos de navegación para recargar cuando se vuelva a esta ruta
    this.routerSubscription = this.router.events.pipe(
      filter(event => event instanceof NavigationEnd && event.url === '/citas')
    ).subscribe(() => {
      // Cuando se navega específicamente a /citas, verificar si el usuario ha cambiado
      const previousUserId = this.currentUser?.usuarioId;
      this.authService.loadUserFromStorage();
      this.currentUser = this.authService.getCurrentUser();
      
      // Si el usuario cambió o si no había usuario y ahora hay uno, recargar las citas
      if ((!previousUserId && this.currentUser) || 
          (previousUserId !== this.currentUser?.usuarioId)) {
        this.loadCitas();
      }
    });
  }
  
  ngOnDestroy(): void {
    if (this.routerSubscription) {
      this.routerSubscription.unsubscribe();
    }
  }
  
  private initializeComponent(): void {
    // Asegurar que el usuario esté cargado desde el storage
    this.authService.loadUserFromStorage();
    this.currentUser = this.authService.getCurrentUser();
    
    // Si no hay usuario pero hay token, intentar cargar el usuario desde el servidor
    if (!this.currentUser && this.authService.getToken()) {
      this.apiService.getCurrentUser().subscribe({
        next: (user) => {
          this.currentUser = {
            usuarioId: user.usuarioId || user.usuario_id,
            nombre: user.nombre || user.name,
            email: user.email,
            rol: user.rol || user.role,
            activo: user.activo !== undefined ? user.activo : true
          };
          this.loadCitas();
          this.loadFiltrosFavoritos();
          if (this.currentUser?.rol === 'ADMINISTRADOR') {
            this.loadEstilistas();
            this.loadServicios();
          }
        },
        error: () => {
          this.loadCitas();
          this.loadFiltrosFavoritos();
        }
      });
    } else {
      this.loadCitas();
      this.loadFiltrosFavoritos();
      if (this.currentUser?.rol === 'ADMINISTRADOR') {
        this.loadEstilistas();
        this.loadServicios();
      }
    }
  }

  loadEstilistas(): void {
    this.apiService.getEstilistas().subscribe({
      next: (data) => {
        this.estilistas = data.filter((u: any) => u.rol === 'ESTILISTA');
      },
      error: (err) => {
        console.error('Error al cargar estilistas:', err);
      }
    });
  }

  loadServicios(): void {
    this.apiService.getServicios().subscribe({
      next: (data) => {
        this.servicios = Array.isArray(data) ? data : (data.content || []);
      },
      error: (err) => {
        console.error('Error al cargar servicios:', err);
      }
    });
  }

  loadFiltrosFavoritos(): void {
    const stored = localStorage.getItem('citas_filtros_favoritos');
    if (stored) {
      try {
        this.filtrosFavoritos = JSON.parse(stored);
      } catch (e) {
        console.error('Error al cargar filtros favoritos:', e);
        this.filtrosFavoritos = [];
      }
    }
  }

  guardarFiltrosFavoritos(): void {
    if (!this.nombreFiltroFavorito.trim()) {
      alert('Por favor, ingresa un nombre para el filtro favorito');
      return;
    }

    const filtro = {
      nombre: this.nombreFiltroFavorito.trim(),
      filtros: {
        fecha: this.filtroFecha,
        estilistaId: this.filtroEstilistaId,
        estado: this.filtroEstado,
        nombreCliente: this.filtroNombreCliente,
        nombreEstilista: this.filtroNombreEstilista,
        nombreServicio: this.filtroNombreServicio
      },
      fechaCreacion: new Date().toISOString()
    };

    this.filtrosFavoritos.push(filtro);
    localStorage.setItem('citas_filtros_favoritos', JSON.stringify(this.filtrosFavoritos));
    this.nombreFiltroFavorito = '';
    alert('Filtro favorito guardado exitosamente');
  }

  aplicarFiltroFavorito(filtro: any): void {
    this.filtroFecha = filtro.filtros.fecha || '';
    this.filtroEstilistaId = filtro.filtros.estilistaId || '';
    this.filtroEstado = filtro.filtros.estado || '';
    this.filtroNombreCliente = filtro.filtros.nombreCliente || '';
    this.filtroNombreEstilista = filtro.filtros.nombreEstilista || '';
    this.filtroNombreServicio = filtro.filtros.nombreServicio || '';
    this.currentPage = 0;
    this.loadCitas();
  }

  eliminarFiltroFavorito(index: number): void {
    if (confirm('¿Estás seguro de que quieres eliminar este filtro favorito?')) {
      this.filtrosFavoritos.splice(index, 1);
      localStorage.setItem('citas_filtros_favoritos', JSON.stringify(this.filtrosFavoritos));
    }
  }

  get canCreate(): boolean {
    return this.currentUser?.rol === 'CLIENTE' || this.currentUser?.rol === 'ADMINISTRADOR';
  }

  loadCitas(): void {
    this.loading = true;
    
    // Asegurar que el usuario actual esté cargado antes de hacer la petición
    if (!this.currentUser) {
      this.authService.loadUserFromStorage();
      this.currentUser = this.authService.getCurrentUser();
    }
    
    const filters: any = {};
    if (this.currentUser?.rol === 'ADMINISTRADOR') {
      if (this.filtroFecha) filters.fecha = this.filtroFecha;
      if (this.filtroEstilistaId) filters.estilistaId = this.filtroEstilistaId;
      if (this.filtroNombreCliente) filters.nombreCliente = this.filtroNombreCliente;
      if (this.filtroNombreEstilista) filters.nombreEstilista = this.filtroNombreEstilista;
      if (this.filtroNombreServicio) filters.nombreServicio = this.filtroNombreServicio;
    }
    if (this.filtroEstado) filters.estado = this.filtroEstado;
    
    this.apiService.getCitas(filters, this.currentPage, this.pageSize, this.usePagination).subscribe({
      next: (data) => {
        if (this.usePagination && data.content) {
          // Respuesta paginada
          this.pageData = data;
          this.citas = data.content;
          this.totalPages = data.totalPages;
          this.totalElements = data.totalElements;
          this.citasFiltradas = this.citas; // Ya vienen filtradas desde backend
        } else {
          // Respuesta lista sin paginar
          this.citas = Array.isArray(data) ? data : [];
          this.aplicarFiltros();
          this.pageData = null;
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error al cargar citas:', err);
        alert('Error al cargar las citas');
        this.loading = false;
      }
    });
  }

  aplicarFiltros(): void {
    if (!this.usePagination) {
      this.citasFiltradas = [...this.citas];
      if (this.filtroEstado) {
        this.citasFiltradas = this.citasFiltradas.filter(c => c.status === this.filtroEstado);
      }
    } else {
      // Con paginación, los filtros se aplican en el backend
      this.currentPage = 0;
      this.loadCitas();
    }
  }

  limpiarFiltros(): void {
    this.filtroFecha = '';
    this.filtroEstilistaId = '';
    this.filtroEstado = '';
    this.filtroNombreCliente = '';
    this.filtroNombreEstilista = '';
    this.filtroNombreServicio = '';
    this.currentPage = 0;
    this.loadCitas();
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadCitas();
  }

  togglePagination(): void {
    this.usePagination = !this.usePagination;
    this.currentPage = 0;
    this.loadCitas();
  }

  editarCita(id: number): void {
    this.router.navigate(['/citas', id, 'editar']);
  }

  cancelarCita(id: number): void {
    if (confirm('¿Estás seguro de que quieres cancelar esta cita?')) {
      this.apiService.updateCita(id, { estado: 'CANCELADA' }).subscribe({
        next: () => {
          this.loadCitas();
        },
        error: (err) => {
          console.error('Error al cancelar cita:', err);
          alert('Error al cancelar la cita: ' + (err.error?.message || 'Error desconocido'));
        }
      });
    }
  }

  canEdit(cita: any): boolean {
    if (!this.currentUser) return false;
    if (this.currentUser.rol === 'ADMINISTRADOR') return true;
    if (this.currentUser.rol === 'CLIENTE' && cita.status === 'CONFIRMADA') return true;
    return false;
  }

  canCancel(cita: any): boolean {
    if (!this.currentUser) return false;
    if (this.currentUser.rol === 'ADMINISTRADOR') return true;
    if (this.currentUser.rol === 'CLIENTE' && cita.status === 'CONFIRMADA') return true;
    return false;
  }

  formatDate(date: string): string {
    if (!date) return '';
    const d = new Date(date);
    return d.toLocaleDateString('es-ES');
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





