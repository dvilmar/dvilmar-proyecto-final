import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService, User } from '../../services/auth.service';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './usuarios.component.html'
})
export class UsuariosComponent implements OnInit {
  usuarios: any[] = [];
  usuariosFiltrados: any[] = [];
  loading = false;
  filtroBusqueda = '';
  mostrarDetalles = false;
  usuarioSeleccionado: any = null;
  
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
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cargarUsuarios();
  }

  cargarUsuarios(): void {
    this.loading = true;
    this.apiService.getAllUsuarios(this.currentPage, this.pageSize, this.usePagination).subscribe({
      next: (data) => {
        if (this.usePagination && data.content) {
          // Respuesta paginada
          this.pageData = data;
          this.usuarios = data.content;
          this.usuariosFiltrados = data.content;
          this.totalPages = data.totalPages;
          this.totalElements = data.totalElements;
        } else {
          // Respuesta lista sin paginar
          this.usuarios = Array.isArray(data) ? data : [];
          this.usuariosFiltrados = this.usuarios;
          this.pageData = null;
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error al cargar usuarios:', err);
        console.error('Error completo:', JSON.stringify(err, null, 2));
        const errorMsg = err.error?.message || err.message || 'Error al cargar los usuarios';
        alert(`Error al cargar los usuarios: ${errorMsg}`);
        this.loading = false;
      }
    });
  }

  filtrarUsuarios(): void {
    if (!this.usePagination) {
      if (!this.filtroBusqueda) {
        this.usuariosFiltrados = this.usuarios;
        return;
      }

      const filtro = this.filtroBusqueda.toLowerCase();
      this.usuariosFiltrados = this.usuarios.filter(u => 
        u.nombre.toLowerCase().includes(filtro) ||
        u.email.toLowerCase().includes(filtro) ||
        u.rol.toLowerCase().includes(filtro)
      );
    } else {
      // Con paginación, el filtro se aplica en el backend (futura mejora)
      // Por ahora, recargar la página
      this.currentPage = 0;
      this.cargarUsuarios();
    }
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.cargarUsuarios();
  }

  togglePagination(): void {
    this.usePagination = !this.usePagination;
    this.currentPage = 0;
    this.cargarUsuarios();
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

  verDetalles(id: number): void {
    this.apiService.getUsuarioById(id).subscribe({
      next: (data) => {
        this.usuarioSeleccionado = data;
        this.mostrarDetalles = true;
      },
      error: (err) => {
        console.error('Error al cargar usuario:', err);
        console.error('Error completo:', JSON.stringify(err, null, 2));
        const errorMsg = err.error?.message || err.message || 'Error al cargar los detalles del usuario';
        alert(`Error al cargar los detalles del usuario: ${errorMsg}`);
      }
    });
  }

  cerrarDetalles(): void {
    this.mostrarDetalles = false;
    this.usuarioSeleccionado = null;
  }

  editarUsuario(id: number): void {
    this.router.navigate(['/usuarios', id, 'editar']);
  }

  toggleActivo(usuario: any): void {
    const accion = usuario.activo ? 'desactivar' : 'activar';
    if (confirm(`¿Estás seguro de que quieres ${accion} a ${usuario.nombre}?`)) {
      this.apiService.toggleUsuarioActivo(usuario.usuarioId).subscribe({
        next: () => {
          usuario.activo = !usuario.activo;
        },
        error: (err) => {
          console.error('Error al cambiar estado:', err);
          console.error('Error completo:', JSON.stringify(err, null, 2));
          const errorMsg = err.error?.message || err.message || 'Error al cambiar el estado del usuario';
          alert(`Error al cambiar el estado del usuario: ${errorMsg}`);
        }
      });
    }
  }
}





