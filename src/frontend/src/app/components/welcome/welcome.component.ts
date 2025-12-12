import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService, User } from '../../services/auth.service';

@Component({
  selector: 'app-welcome',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './welcome.component.html',
  styleUrls: ['./welcome.component.css']
})
export class WelcomeComponent implements OnInit {
  currentUser: User | null = null;
  isAuthenticated = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Validar autenticación correctamente
    this.validateAuthentication();
    
    // Suscribirse a cambios futuros
    this.authService.currentUser$.subscribe(user => {
      this.validateAuthentication();
    });
  }

  private validateAuthentication(): void {
    // Verificar si está realmente autenticado (con token y usuario válido)
    this.isAuthenticated = this.authService.isAuthenticated();
    if (this.isAuthenticated) {
      this.currentUser = this.authService.getCurrentUser();
    } else {
      // Si no está autenticado correctamente, limpiar el estado
      this.currentUser = null;
    }
  }

  navigateToDashboard(): void {
    // Todos los usuarios autenticados pueden quedarse en la página principal
    // El navbar les mostrará las opciones según su rol
    // Este método ya no es necesario pero lo dejamos por si se usa en el HTML
  }

  getRoleSpecificFeatures(): string[] {
    const role = this.currentUser?.rol;
    switch (role) {
      case 'ADMINISTRADOR':
        return [
          'Gestión completa de usuarios',
          'Control de disponibilidad de estilistas',
          'Gestión de excepciones de horario',
          'Vista global de todas las citas',
          'Administración del sistema'
        ];
      case 'ESTILISTA':
        return [
          'Gestionar servicios que ofreces',
          'Consultar citas programadas',
          'Ver calendario de disponibilidad',
          'Actualizar tu perfil profesional'
        ];
      case 'CLIENTE':
        return [
          'Reservar citas con estilistas',
          'Cancelar o modificar citas',
          'Gestionar tu perfil personal',
          'Recibir recordatorios de citas'
        ];
      default:
        return [
          'Reserva citas fácilmente',
          'Gestiona tu perfil',
          'Recibe recordatorios',
          'Accede desde cualquier dispositivo'
        ];
    }
  }

  getRoleSpecificActions(): { label: string, route: string, description: string }[] {
    const role = this.currentUser?.rol;
    
    if (!role) {
      return [];
    }

    const actions: { label: string, route: string, description: string }[] = [];

    switch (role) {
      case 'ADMINISTRADOR':
        actions.push(
          { label: 'Panel de Administración', route: '/admin', description: 'Accede al panel de control' },
          { label: 'Gestión de Usuarios', route: '/usuarios', description: 'Administra usuarios del sistema' },
          { label: 'Excepciones de Horario', route: '/excepciones', description: 'Gestiona fechas especiales' }
        );
        break;
      case 'ESTILISTA':
        actions.push(
          { label: 'Mis Citas', route: '/citas', description: 'Consulta tus citas' },
          { label: 'Mis Servicios', route: '/servicios', description: 'Gestiona servicios que ofreces' },
          { label: 'Disponibilidad', route: '/disponibilidad', description: 'Configura tu horario' },
          { label: 'Mi Perfil', route: '/perfil', description: 'Actualiza tu información' }
        );
        break;
      case 'CLIENTE':
        actions.push(
          { label: 'Nueva Cita', route: '/citas/nueva', description: 'Reserva una cita' },
          { label: 'Mis Citas', route: '/citas', description: 'Ver y gestionar citas' },
          { label: 'Mi Perfil', route: '/perfil', description: 'Gestionar datos personales' }
        );
        break;
    }

    return actions;
  }
}




