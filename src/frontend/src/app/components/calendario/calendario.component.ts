import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService, User } from '../../services/auth.service';

@Component({
  selector: 'app-calendario',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './calendario.component.html',
  styleUrls: ['./calendario.component.css']
})
export class CalendarioComponent implements OnInit {
  currentUser: User | null = null;
  citas: any[] = [];
  disponibilidades: any[] = [];
  excepciones: any[] = [];
  estilistas: any[] = [];
  estilistaSeleccionadoId: number | null = null;
  fechaActual = new Date();
  anio = this.fechaActual.getFullYear();
  mes = this.fechaActual.getMonth();
  diasSemana = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];
  semanas: (Date | null)[][] = [];
  meses = [
    'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
    'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
  ];

  get nombreMes(): string {
    return this.meses[this.mes];
  }

  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.cargarCitas();
    this.cargarDisponibilidades();
    this.cargarExcepciones();
    if (this.currentUser?.rol === 'CLIENTE' || this.currentUser?.rol === 'ADMINISTRADOR') {
      this.cargarEstilistas();
    }
  }

  cargarEstilistas(): void {
    this.apiService.getEstilistas().subscribe({
      next: (data) => {
        this.estilistas = data.filter((u: any) => u.rol === 'ESTILISTA' && u.activo);
      },
      error: (err) => {
        console.error('Error al cargar estilistas:', err);
      }
    });
  }

  cargarCitas(): void {
    const filters: any = {};
    if (this.estilistaSeleccionadoId) {
      filters.estilistaId = this.estilistaSeleccionadoId;
    }
    
    this.apiService.getCitas(filters).subscribe({
      next: (data) => {
        this.citas = data;
        this.generarCalendario();
      },
      error: (err) => {
        console.error('Error al cargar citas:', err);
      }
    });
  }

  cargarDisponibilidades(): void {
    const estilistaId = this.estilistaSeleccionadoId || undefined;
    this.apiService.getDisponibilidades(estilistaId).subscribe({
      next: (data) => {
        this.disponibilidades = Array.isArray(data) ? data : (data.content || []);
        this.generarCalendario();
      },
      error: (err) => {
        console.error('Error al cargar disponibilidades:', err);
      }
    });
  }

  cargarExcepciones(): void {
    const filters: any = {};
    if (this.estilistaSeleccionadoId) {
      filters.estilistaId = this.estilistaSeleccionadoId;
    }
    
    this.apiService.getExcepciones(filters).subscribe({
      next: (data) => {
        this.excepciones = Array.isArray(data) ? data : [];
        this.generarCalendario();
      },
      error: (err) => {
        console.error('Error al cargar excepciones:', err);
      }
    });
  }

  cambiarEstilista(event: any): void {
    const estilistaId = event.target.value ? Number(event.target.value) : null;
    this.estilistaSeleccionadoId = estilistaId;
    this.cargarDatos();
  }

  generarCalendario(): void {
    this.semanas = [];
    const primerDia = new Date(this.anio, this.mes, 1);
    const ultimoDia = new Date(this.anio, this.mes + 1, 0);
    const primerDiaSemana = (primerDia.getDay() + 6) % 7; // Lunes = 0

    let semana: (Date | null)[] = [];
    
    // Días del mes anterior
    for (let i = 0; i < primerDiaSemana; i++) {
      semana.push(null);
    }

    // Días del mes actual
    for (let dia = 1; dia <= ultimoDia.getDate(); dia++) {
      semana.push(new Date(this.anio, this.mes, dia));
      if (semana.length === 7) {
        this.semanas.push(semana);
        semana = [];
      }
    }

    // Días del mes siguiente
    while (semana.length < 7) {
      semana.push(null);
    }
    if (semana.some(d => d !== null)) {
      this.semanas.push(semana);
    }
  }

  mesAnterior(): void {
    this.mes--;
    if (this.mes < 0) {
      this.mes = 11;
      this.anio--;
    }
    this.generarCalendario();
  }

  mesSiguiente(): void {
    this.mes++;
    if (this.mes > 11) {
      this.mes = 0;
      this.anio++;
    }
    this.generarCalendario();
  }

  esHoy(dia: Date | null): boolean {
    if (!dia) return false;
    const hoy = new Date();
    return dia.getDate() === hoy.getDate() &&
           dia.getMonth() === hoy.getMonth() &&
           dia.getFullYear() === hoy.getFullYear();
  }

  tieneCitas(dia: Date | null): boolean {
    if (!dia) return false;
    return this.getCitasDelDia(dia).length > 0;
  }

  getCitasDelDia(dia: Date | null): any[] {
    if (!dia) return [];
    const fechaStr = this.formatearFecha(dia);
    return this.citas.filter(cita => {
      const citaFecha = new Date(cita.date);
      return citaFecha.toISOString().split('T')[0] === fechaStr;
    });
  }

  formatearFecha(fecha: Date): string {
    return fecha.toISOString().split('T')[0];
  }

  getCurrentMonthValue(): string {
    return `${this.anio}-${String(this.mes + 1).padStart(2, '0')}`;
  }

  cambiarMesDesdeInput(event: any): void {
    const value = event.target.value;
    if (value) {
      const [year, month] = value.split('-').map(Number);
      this.anio = year;
      this.mes = month - 1;
      this.generarCalendario();
    }
  }

  // Obtener el día de la semana como número (0=Lunes, 6=Domingo)
  getDayOfWeek(date: Date): number {
    const day = date.getDay();
    return day === 0 ? 6 : day - 1; // Convertir domingo de 0 a 6
  }

  // Verificar si un día tiene disponibilidad (trabaja algún estilista ese día)
  tieneDisponibilidad(dia: Date | null): boolean {
    if (!dia) return false;
    
    const dayOfWeek = this.getDayOfWeek(dia);
    const dayNames = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
    const dayName = dayNames[dayOfWeek];
    
    // Si hay un estilista seleccionado, verificar solo para ese estilista
    if (this.estilistaSeleccionadoId) {
      return this.disponibilidades.some((disp: any) => 
        disp.stylistId === this.estilistaSeleccionadoId && 
        disp.dayOfWeek === dayName
      );
    }
    
    // Si no hay estilista seleccionado, verificar si algún estilista trabaja ese día
    return this.disponibilidades.some((disp: any) => disp.dayOfWeek === dayName);
  }

  // Verificar si hay una excepción que hace que el día no esté disponible
  tieneExcepcionNoDisponible(dia: Date | null): boolean {
    if (!dia) return false;
    
    const fechaStr = this.formatearFecha(dia);
    
    return this.excepciones.some((excepcion: any) => {
      const excepcionFecha = new Date(excepcion.date).toISOString().split('T')[0];
      
      // Verificar si es una excepción NO_DISPONIBLE para este día
      if (excepcionFecha === fechaStr && excepcion.type === 'NO_DISPONIBLE') {
        // Si hay estilista seleccionado, verificar si la excepción es para ese estilista o para todos
        if (this.estilistaSeleccionadoId) {
          return !excepcion.stylistId || excepcion.stylistId === this.estilistaSeleccionadoId;
        }
        // Si no hay estilista seleccionado, incluir si es para todos los estilistas
        return !excepcion.stylistId || true;
      }
      
      return false;
    });
  }

  // Verificar si un día está disponible para reservas
  estaDisponible(dia: Date | null): boolean {
    if (!dia) return false;
    
    // No permitir fechas pasadas
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    const fechaDia = new Date(dia);
    fechaDia.setHours(0, 0, 0, 0);
    
    if (fechaDia < hoy) {
      return false;
    }
    
    // Verificar si tiene disponibilidad (alguno trabaja ese día)
    if (!this.tieneDisponibilidad(dia)) {
      return false;
    }
    
    // Verificar si hay una excepción que hace que no esté disponible
    if (this.tieneExcepcionNoDisponible(dia)) {
      return false;
    }
    
    return true;
  }

  // Obtener la razón por la que un día no está disponible
  getRazonNoDisponible(dia: Date | null): string {
    if (!dia) return '';
    
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    const fechaDia = new Date(dia);
    fechaDia.setHours(0, 0, 0, 0);
    
    if (fechaDia < hoy) {
      return 'Fecha pasada';
    }
    
    if (!this.tieneDisponibilidad(dia)) {
      return 'No se trabaja este día';
    }
    
    if (this.tieneExcepcionNoDisponible(dia)) {
      const fechaStr = this.formatearFecha(dia);
      const excepcion = this.excepciones.find((exc: any) => {
        const excFecha = new Date(exc.date).toISOString().split('T')[0];
        return excFecha === fechaStr && exc.type === 'NO_DISPONIBLE';
      });
      return excepcion?.reason || 'Excepción de horario';
    }
    
    return '';
  }

  get canCreateAppointment(): boolean {
    return this.currentUser?.rol === 'CLIENTE' || this.currentUser?.rol === 'ADMINISTRADOR';
  }

  seleccionarFecha(dia: Date | null): void {
    // Esta funcionalidad ahora está solo en el formulario de citas
    // El componente de calendario independiente ya no se usa
    if (!dia || !this.canCreateAppointment) return;
    
    // Verificar si el día está disponible
    if (!this.estaDisponible(dia)) {
      const razon = this.getRazonNoDisponible(dia);
      alert(`No se puede reservar este día: ${razon}`);
      return;
    }
    
    const fechaStr = this.formatearFecha(dia);
    
    // Navegar al formulario de nueva cita con la fecha preseleccionada
    const queryParams: any = { fecha: fechaStr };
    if (this.estilistaSeleccionadoId) {
      queryParams.estilistaId = this.estilistaSeleccionadoId;
    }
    
    this.router.navigate(['/citas/nueva'], {
      queryParams: queryParams
    });
  }

  verCita(citaId: number): void {
    this.router.navigate(['/citas', citaId, 'editar']);
  }
}





