import { Routes } from '@angular/router';
import { LoginComponent } from './components/auth/login/login.component';
// RegisterComponent eliminado - solo el admin crea usuarios desde /usuarios/nuevo
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { AdminDashboardComponent } from './components/admin/admin-dashboard/admin-dashboard.component';
import { CitasListComponent } from './components/citas/citas-list/citas-list.component';
import { CitaFormComponent } from './components/citas/cita-form/cita-form.component';
import { HistorialCitasComponent } from './components/citas/historial-citas/historial-citas.component';
// CalendarioComponent ya no se usa como ruta independiente - solo en el formulario de citas
import { DisponibilidadComponent } from './components/disponibilidad/disponibilidad.component';
import { ExcepcionesComponent } from './components/excepciones/excepciones.component';
import { ServiciosComponent } from './components/servicios/servicios.component';
import { PerfilComponent } from './components/perfil/perfil.component';
import { UsuariosComponent } from './components/usuarios/usuarios.component';
import { UsuarioFormComponent } from './components/usuarios/usuario-form/usuario-form.component';
import { NotificacionesComponent } from './components/notificaciones/notificaciones.component';
import { WelcomeComponent } from './components/welcome/welcome.component';
import { authGuard } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';

export const routes: Routes = [
  { path: '', component: WelcomeComponent },
  { path: 'login', component: LoginComponent },
  // Ruta de registro eliminada - solo el admin puede crear usuarios
  { 
    path: 'dashboard', 
    component: DashboardComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ESTILISTA', 'ADMINISTRADOR'] }
  },
  { 
    path: 'admin', 
    component: AdminDashboardComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMINISTRADOR'] }
  },
  { 
    path: 'citas', 
    component: CitasListComponent,
    canActivate: [authGuard]
    // Todos los usuarios autenticados pueden ver citas, pero el componente filtra por rol
  },
  { 
    path: 'citas/nueva', 
    component: CitaFormComponent
    // Sin authGuard - permite crear citas sin autenticación
  },
  { 
    path: 'citas/:id/editar', 
    component: CitaFormComponent,
    canActivate: [authGuard]
  },
  { 
    path: 'citas/historial', 
    component: HistorialCitasComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['CLIENTE'] }
  },
  // Ruta de calendario eliminada - el calendario solo se muestra en el formulario de reserva
  { 
    path: 'disponibilidad', 
    component: DisponibilidadComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ESTILISTA', 'ADMINISTRADOR'] }
  },
  { 
    path: 'excepciones', 
    component: ExcepcionesComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMINISTRADOR'] }
  },
  { 
    path: 'servicios', 
    component: ServiciosComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ESTILISTA', 'ADMINISTRADOR'] }
  },
  { 
    path: 'perfil', 
    component: PerfilComponent,
    canActivate: [authGuard]
  },
  { 
    path: 'usuarios', 
    component: UsuariosComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMINISTRADOR'] }
  },
  { 
    path: 'usuarios/nuevo', 
    component: UsuarioFormComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMINISTRADOR'] }
  },
  { 
    path: 'usuarios/:id/editar', 
    component: UsuarioFormComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMINISTRADOR'] }
  },
  { 
    path: 'notificaciones', 
    component: NotificacionesComponent,
    canActivate: [authGuard]
    // Todos los usuarios autenticados pueden ver notificaciones
  },
  { path: '**', redirectTo: '/login' }
];






