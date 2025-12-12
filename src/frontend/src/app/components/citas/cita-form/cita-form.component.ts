import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ApiService } from '../../../services/api.service';
import { AuthService, User } from '../../../services/auth.service';
import { ServiceOffer, Stylist, Client, Availability, ScheduleException, Appointment } from '../../../types/models';

@Component({
  selector: 'app-cita-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './cita-form.component.html',
  styleUrls: ['./cita-form.component.css']
})
export class CitaFormComponent implements OnInit {
  citaForm: FormGroup;
  currentUser: User | null = null;
  isEditMode = false;
  citaId: number | null = null;
  servicios: ServiceOffer[] = [];
  estilistas: Stylist[] = [];
  clientes: Client[] = [];
  serviciosSeleccionados: number[] = [];
  precioTotal = 0;
  loading = false;
  errorMessage = '';
  successMessage = '';
  minDate = new Date().toISOString().split('T')[0];
  citaActual: Appointment | null = null;
  
  // Propiedades para el calendario
  fechaActual = new Date();
  anio = this.fechaActual.getFullYear();
  mes = this.fechaActual.getMonth();
  diasSemana = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];
  semanas: (Date | null)[][] = [];
  meses = [
    'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
    'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
  ];
  disponibilidades: Availability[] = [];
  excepciones: ScheduleException[] = [];
  mostrarCalendario = true;
  fechaSeleccionada: Date | null = null;
  horasDisponibles: string[] = [];
  citasDelDia: Appointment[] = [];
  diasSinHoras: Set<string> = new Set(); // Para trackear días que no tienen horas disponibles
  cargandoHoras = false;

  constructor(
    private fb: FormBuilder,
    private apiService: ApiService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.citaForm = this.fb.group({
      date: ['', Validators.required],
      startTime: ['', Validators.required],
      endTime: ['', Validators.required],
      clientId: [''],
      stylistId: ['', Validators.required],
      clientPhone: [''],
      // Campos para reserva pública (sin autenticación)
      clientName: [''],
      clientEmail: [''],
      clientPassword: ['']
    });
  }

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    
    // Si el usuario está autenticado y es CLIENTE, usar su ID
    if (this.currentUser?.rol === 'CLIENTE') {
      this.citaForm.patchValue({ clientId: this.currentUser.usuarioId });
      // Hacer campos de cliente requeridos si no hay autenticación
      this.citaForm.get('clientName')?.clearValidators();
      this.citaForm.get('clientEmail')?.clearValidators();
    } else if (!this.currentUser) {
      // Si no hay usuario autenticado, hacer campos de cliente requeridos
      this.citaForm.get('clientName')?.setValidators([Validators.required]);
      this.citaForm.get('clientEmail')?.setValidators([Validators.required, Validators.email]);
      this.citaForm.get('clientPhone')?.setValidators([Validators.required, this.phoneValidator]);
      this.citaForm.get('clientPassword')?.setValidators([Validators.required, Validators.minLength(6)]);
      this.citaForm.get('clientId')?.clearValidators();
    }
    
    this.citaForm.get('clientName')?.updateValueAndValidity();
    this.citaForm.get('clientEmail')?.updateValueAndValidity();
    this.citaForm.get('clientPhone')?.updateValueAndValidity();

    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.citaId = +params['id'];
        this.loadCita();
      }
    });

    // Verificar si hay una fecha en los query params (viene del calendario)
    this.route.queryParams.subscribe(params => {
      if (params['fecha'] && !this.isEditMode) {
        const fecha = params['fecha'];
        const fechaObj = new Date(fecha + 'T00:00:00');
        this.fechaSeleccionada = fechaObj;
        this.citaForm.patchValue({ date: fecha });
        // Navegar al mes de la fecha
        this.anio = fechaObj.getFullYear();
        this.mes = fechaObj.getMonth();
      }
    });

    this.loadServicios();
    this.loadEstilistas();
    if (this.currentUser?.rol === 'ADMINISTRADOR') {
      this.loadClientes();
    }
    
    // Cargar datos del calendario después de cargar estilistas
    setTimeout(() => {
      this.loadDisponibilidades();
      this.loadExcepciones();
      this.generarCalendario();
    }, 100);
    
    // Si hay una fecha en el formulario, navegar al mes correspondiente
    this.citaForm.get('date')?.valueChanges.subscribe(date => {
      if (date) {
        const fecha = new Date(date + 'T00:00:00');
        this.anio = fecha.getFullYear();
        this.mes = fecha.getMonth();
        this.generarCalendario();
      }
    });
    
    // Si hay un estilista seleccionado, recargar disponibilidades
    this.citaForm.get('stylistId')?.valueChanges.subscribe(stylistId => {
      if (stylistId) {
        // Limpiar días sin horas y fecha seleccionada al cambiar estilista
        this.diasSinHoras.clear();
        this.fechaSeleccionada = null;
        this.horasDisponibles = [];
        this.citaForm.patchValue({ date: '', startTime: '', endTime: '' });
        this.loadDisponibilidades();
        this.loadExcepciones();
      } else {
        this.disponibilidades = [];
        this.excepciones = [];
        this.horasDisponibles = [];
        this.fechaSeleccionada = null;
        this.diasSinHoras.clear();
        this.citaForm.patchValue({ date: '', startTime: '', endTime: '' });
        this.generarCalendario();
      }
    });

    // Si cambia la fecha, recargar horas disponibles
    this.citaForm.get('date')?.valueChanges.subscribe(date => {
      if (date && this.citaForm.get('stylistId')?.value) {
        const fecha = new Date(date + 'T00:00:00');
        this.fechaSeleccionada = fecha;
        this.cargarHorasDisponibles(fecha);
      } else {
        this.horasDisponibles = [];
        this.fechaSeleccionada = null;
      }
    });
  }

  loadCita(): void {
    if (!this.citaId) return;
    this.apiService.getCitaById(this.citaId).subscribe({
      next: (cita) => {
        this.citaActual = cita;
        this.citaForm.patchValue({
          date: cita.date,
          startTime: cita.startTime,
          endTime: cita.endTime,
          clientId: cita.clientId,
          stylistId: cita.stylistId,
          clientPhone: cita.clientPhone
        });
        if (cita.services) {
          this.serviciosSeleccionados = cita.services?.map((s: ServiceOffer) => s.serviceId) || [];
          this.calcularPrecioTotal();
        }
        // Navegar al mes de la fecha de la cita
        if (cita.date) {
          const fecha = new Date(cita.date + 'T00:00:00');
          this.fechaSeleccionada = fecha;
          this.anio = fecha.getFullYear();
          this.mes = fecha.getMonth();
          this.loadDisponibilidades();
          this.loadExcepciones();
          setTimeout(() => {
            this.generarCalendario();
            if (cita.stylistId) {
              this.cargarHorasDisponibles(fecha);
            }
          }, 100);
        }
      },
      error: (err) => {
        console.error('Error al cargar cita:', err);
        alert('Error al cargar la cita');
        this.router.navigate(['/citas']);
      }
    });
  }

  canCancelar(): boolean {
    if (!this.citaActual) return false;
    // Solo se puede cancelar si la cita está confirmada
    return this.citaActual.status === 'CONFIRMADA';
  }

  cancelarCita(): void {
    if (!this.citaId || !this.canCancelar()) return;
    
    if (confirm('¿Estás seguro de que quieres cancelar esta cita?')) {
      this.loading = true;
      this.apiService.updateCita(this.citaId, { estado: 'CANCELADA' }).subscribe({
        next: () => {
          alert('Cita cancelada correctamente');
          this.router.navigate(['/citas']);
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Error al cancelar la cita';
          this.loading = false;
        }
      });
    }
  }

  loadServicios(): void {
    this.apiService.getServicios().subscribe({
      next: (data) => {
        this.servicios = data;
      },
      error: (err) => {
        console.error('Error al cargar servicios:', err);
      }
    });
  }

  loadEstilistas(): void {
    this.apiService.getEstilistas().subscribe({
      next: (data) => {
        this.estilistas = data.filter((u: Stylist) => u.rol === 'ESTILISTA' && u.activo) as Stylist[];
        // Limpiar días sin horas cuando se cargan estilistas
        this.diasSinHoras.clear();
      },
      error: (err) => {
        console.error('Error al cargar estilistas:', err);
      }
    });
  }

  loadClientes(): void {
    // Solo cargar clientes si el usuario es administrador
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser || currentUser.rol !== 'ADMINISTRADOR') {
      console.warn('Solo los administradores pueden cargar la lista de clientes');
      return;
    }
    
    this.apiService.getAllUsuarios().subscribe({
      next: (data) => {
        // Manejar tanto arrays como objetos paginados
        const usuarios = Array.isArray(data) ? data : (data.content || []);
        this.clientes = usuarios.filter((u: any) => u.rol === 'CLIENTE') as Client[];
      },
      error: (err) => {
        console.error('Error al cargar clientes:', err);
        console.error('Error completo:', JSON.stringify(err, null, 2));
      }
    });
  }

  toggleServicio(serviceId: number, event: Event): void {
    const target = event.target as HTMLInputElement;
    if (target && target.checked) {
      this.serviciosSeleccionados.push(serviceId);
    } else {
      this.serviciosSeleccionados = this.serviciosSeleccionados.filter(id => id !== serviceId);
    }
    this.calcularPrecioTotal();
    
    // Si hay una hora seleccionada, recalcular la hora de fin
    const horaInicio = this.citaForm.get('startTime')?.value;
    if (horaInicio && this.fechaSeleccionada) {
      const serviciosSeleccionados = this.servicios.filter(s => 
        this.serviciosSeleccionados.includes(s.serviceId)
      );
      const duracionTotal = serviciosSeleccionados.reduce((sum, s) => sum + (s.duration || 0), 0);
      const minutosInicio = this.parseTime(horaInicio);
      const minutosFin = minutosInicio + (duracionTotal || 60);
      const horaFin = this.formatTime(minutosFin);
      this.citaForm.patchValue({ endTime: horaFin });
    }
  }

  isServicioSeleccionado(serviceId: number): boolean {
    return this.serviciosSeleccionados.includes(serviceId);
  }

  calcularPrecioTotal(): void {
    this.precioTotal = this.servicios
      .filter(s => this.serviciosSeleccionados.includes(s.serviceId))
      .reduce((sum, s) => sum + (s.unitPrice || 0), 0);
  }

  onSubmit(): void {
    if (this.citaForm.invalid) {
      this.markFormGroupTouched(this.citaForm);
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const formValue = this.citaForm.value;
    const citaData: any = {
      date: formValue.date,
      startTime: formValue.startTime,
      endTime: formValue.endTime,
      clientId: formValue.clientId || this.currentUser?.usuarioId,
      stylistId: formValue.stylistId,
      serviceIds: this.serviciosSeleccionados,
      clientPhone: formValue.clientPhone || null
    };

    if (this.isEditMode && this.citaId) {
      this.apiService.updateCita(this.citaId, {
        fecha: citaData.date,
        horaInicio: citaData.startTime,
        horaFin: citaData.endTime
      }).subscribe({
        next: () => {
          this.router.navigate(['/citas']);
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Error al actualizar la cita';
          this.loading = false;
        }
      });
    } else {
      // Determinar si es creación pública o autenticada
      // Si hay un usuario autenticado (incluso CLIENTE), usar endpoint autenticado
      const isPublic = !this.currentUser;
      
      let appointmentData: any;
      if (isPublic) {
        // Crear usando endpoint público (usuario no autenticado)
        appointmentData = {
          clientName: formValue.clientName,
          clientEmail: formValue.clientEmail,
          clientPhone: formValue.clientPhone || '',
          clientPassword: formValue.clientPassword || '',
          stylistId: formValue.stylistId,
          date: formValue.date,
          startTime: formValue.startTime,
          endTime: formValue.endTime,
          serviceIds: this.serviciosSeleccionados
        };
      } else {
        // Crear usando endpoint autenticado (usuario CLIENTE autenticado o ADMINISTRADOR)
        appointmentData = citaData;
      }
      
      this.apiService.createCita(appointmentData, isPublic).subscribe({
        next: (response) => {
          console.log('Respuesta de creación de cita:', response);
          this.successMessage = '¡Cita creada exitosamente!';
          this.errorMessage = '';
          
          // Si se recibió un token (nuevo usuario con contraseña), hacer login automático
          if (response.token && response.auth) {
            console.log('Token recibido, iniciando login automático');
            console.log('Datos de auth recibidos:', response.auth);
            
            // Extraer datos del usuario de la respuesta
            // El backend envía: userId, name, email, role (enum)
            const authData = response.auth;
            const usuarioId = authData.usuarioId || authData.userId;
            const nombre = authData.name || authData.nombre;
            const email = authData.email || '';
            // El role puede venir como enum o como string
            let rol: string;
            if (authData.rol) {
              rol = typeof authData.rol === 'string' ? authData.rol : (authData.rol.name || String(authData.rol));
            } else if (authData.role) {
              rol = typeof authData.role === 'string' ? authData.role : (authData.role.name || String(authData.role));
            } else {
              rol = 'CLIENTE'; // Por defecto si no viene
            }
            
            // Guardar token y datos del usuario directamente en localStorage
            localStorage.setItem('token', response.token);
            const user = {
              usuarioId: usuarioId,
              nombre: nombre,
              email: email,
              rol: rol.toUpperCase(), // Asegurar que esté en mayúsculas
              activo: true
            };
            localStorage.setItem('user', JSON.stringify(user));
            console.log('Usuario guardado en localStorage:', user);
            
            // Actualizar el usuario en el servicio de autenticación
            this.authService.setCurrentUser(user);
            this.currentUser = user;
            console.log('Usuario actual después de setCurrentUser:', this.currentUser);
            
            // Esperar un momento para asegurar que todo esté sincronizado
            setTimeout(() => {
              // Navegar a citas
              this.router.navigate(['/citas']).then((success) => {
                if (success) {
                  console.log('Navegación exitosa a /citas');
                  // Recargar el usuario una vez más después de navegar para asegurar sincronización
                  this.authService.loadUserFromStorage();
                } else {
                  console.error('Error al navegar a /citas');
                }
              });
            }, 500);
          } else {
            console.log('No se recibió token, respuesta:', response);
            // Si ya había un usuario, simplemente navegar
            if (this.currentUser) {
              setTimeout(() => {
                this.router.navigate(['/citas']);
              }, 1500);
            } else {
              // Sin usuario ni token, volver al inicio
              setTimeout(() => {
                this.router.navigate(['/']);
              }, 1500);
            }
          }
          this.loading = false;
        },
        error: (err) => {
          console.error('Error al crear cita:', err);
          this.errorMessage = err.error?.message || 'Error al crear la cita';
          this.successMessage = '';
          this.loading = false;
        }
      });
    }
  }

  cancelar(): void {
    this.router.navigate(['/citas']);
  }

  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();
    });
  }

  // Validador personalizado para teléfono (mismo patrón que el backend)
  private phoneValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return null; // Dejamos que required se encargue del campo vacío
    }
    const phone = control.value.toString().trim();
    // Eliminar espacios para validar (como el backend)
    const cleanPhone = phone.replace(/\s/g, '');
    // Patrón: 9 dígitos comenzando con 6-9 (móviles españoles), opcional prefijo +34 o 0034
    const phonePattern = /^(\+34|0034)?[6-9][0-9]{8}$/;
    
    if (!phonePattern.test(cleanPhone)) {
      return { invalidPhone: true };
    }
    return null;
  }

  // Métodos del calendario
  get nombreMes(): string {
    return this.meses[this.mes];
  }

  loadDisponibilidades(): void {
    const stylistId = this.citaForm.get('stylistId')?.value || undefined;
    // Limpiar días sin horas cuando se cargan disponibilidades
    this.diasSinHoras.clear();
    this.apiService.getDisponibilidades(stylistId).subscribe({
      next: (data) => {
        this.disponibilidades = Array.isArray(data) ? data : (data.content || []);
        // Regenerar calendario después de cargar disponibilidades
        this.generarCalendario();
      },
      error: (err) => {
        console.error('Error al cargar disponibilidades:', err);
        this.disponibilidades = [];
        this.generarCalendario();
      }
    });
  }

  loadExcepciones(): void {
    const stylistId = this.citaForm.get('stylistId')?.value || undefined;
    this.apiService.getExcepciones({ estilistaId: stylistId }).subscribe({
      next: (data) => {
        this.excepciones = Array.isArray(data) ? data : [];
        // Regenerar calendario después de cargar excepciones
        this.generarCalendario();
      },
      error: (err) => {
        console.error('Error al cargar excepciones:', err);
        this.excepciones = [];
        this.generarCalendario();
      }
    });
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

  getDayOfWeek(date: Date): number {
    const day = date.getDay();
    return day === 0 ? 6 : day - 1;
  }

  tieneDisponibilidad(dia: Date | null): boolean {
    if (!dia) return false;
    
    const stylistId = this.citaForm.get('stylistId')?.value;
    
    // Si no hay estilista seleccionado, no hay disponibilidad
    if (!stylistId) {
      return false;
    }
    
    // Si no hay disponibilidades cargadas, no hay disponibilidad
    if (!this.disponibilidades || this.disponibilidades.length === 0) {
      return false;
    }
    
    // Obtener el día de la semana (0=domingo, 1=lunes, etc. en JavaScript)
    const jsDay = dia.getDay();
    // Mapear a nombres de días: Domingo=0→SUNDAY, Lunes=1→MONDAY, etc.
    const dayNamesMap: { [key: number]: string } = {
      0: 'SUNDAY',    // Domingo
      1: 'MONDAY',    // Lunes
      2: 'TUESDAY',   // Martes
      3: 'WEDNESDAY', // Miércoles
      4: 'THURSDAY',  // Jueves
      5: 'FRIDAY',    // Viernes
      6: 'SATURDAY'   // Sábado
    };
    const dayName = dayNamesMap[jsDay];
    
    if (!dayName) {
      console.warn('Día de la semana no reconocido:', jsDay);
      return false;
    }
    
    // Convertir ambos a número para comparación correcta
    const stylistIdNum = Number(stylistId);
    
    const encontrada = this.disponibilidades.some((disp: Availability) => {
      const dispStylistId = Number(disp.stylistId || 0);
      // El enum puede venir como string "MONDAY" o como objeto, normalizar a string y convertir a mayúsculas
      let dispDayOfWeek: string = '';
      if (disp.dayOfWeek) {
        if (typeof disp.dayOfWeek === 'object' && disp.dayOfWeek !== null) {
          dispDayOfWeek = String(disp.dayOfWeek).toUpperCase();
        } else {
          dispDayOfWeek = String(disp.dayOfWeek).toUpperCase();
        }
      }
      const matches = dispStylistId === stylistIdNum && dispDayOfWeek === dayName;
      return matches;
    });
    
    return encontrada;
  }

  tieneExcepcionNoDisponible(dia: Date | null): boolean {
    if (!dia) return false;
    
    const fechaStr = this.formatearFecha(dia);
    const stylistId = this.citaForm.get('stylistId')?.value;
    
    if (!stylistId) {
      return false;
    }
    
    const stylistIdNum = Number(stylistId);
    
    return this.excepciones.some((excepcion: ScheduleException) => {
      const excepcionFecha = new Date(excepcion.date).toISOString().split('T')[0];
      
      if (excepcionFecha === fechaStr && excepcion.type === 'NO_DISPONIBLE') {
        const excepcionStylistId = excepcion.stylistId;
        // Si no tiene estilista específico, aplica a todos
        if (!excepcionStylistId) {
          return true;
        }
        // Si tiene estilista específico, comparar
        return Number(excepcionStylistId) === stylistIdNum;
      }
      
      return false;
    });
  }

  estaDisponible(dia: Date | null): boolean {
    if (!dia) return false;
    
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    const fechaDia = new Date(dia);
    fechaDia.setHours(0, 0, 0, 0);
    
    // No permitir fechas pasadas
    if (fechaDia < hoy) {
      return false;
    }
    
    // Verificar disponibilidad básica del día
    if (!this.tieneDisponibilidad(dia)) {
      return false;
    }
    
    // Verificar excepciones
    if (this.tieneExcepcionNoDisponible(dia)) {
      return false;
    }
    
    // Si sabemos que este día no tiene horas disponibles, marcarlo como no disponible
    const fechaStr = this.formatearFecha(dia);
    if (this.diasSinHoras.has(fechaStr)) {
      return false;
    }
    
    // Nota: No verificamos horas disponibles aquí porque sería costoso
    // La verificación de horas se hace al seleccionar el día en seleccionarFecha()
    return true;
  }

  formatearFecha(fecha: Date): string {
    // Usar métodos locales para evitar problemas de zona horaria
    const year = fecha.getFullYear();
    const month = String(fecha.getMonth() + 1).padStart(2, '0');
    const day = String(fecha.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  seleccionarFecha(dia: Date | null): void {
    if (!dia) {
      return;
    }
    
    const stylistId = this.citaForm.get('stylistId')?.value;
    if (!stylistId) {
      return;
    }
    
    // Verificar disponibilidad básica del día (sin horas aún)
    if (!this.estaDisponible(dia)) {
      return;
    }
    
    // Verificar si ya sabemos que este día no tiene horas disponibles
    const fechaStr = this.formatearFecha(dia);
    if (this.diasSinHoras.has(fechaStr)) {
      // No hacer nada, simplemente no permitir la selección
      return;
    }
    
    // Cargar horas disponibles primero, y solo seleccionar la fecha si hay horas disponibles
    this.cargarHorasDisponibles(dia, (tieneHoras: boolean) => {
      if (tieneHoras) {
        // Hay horas disponibles, seleccionar la fecha
        this.fechaSeleccionada = dia;
        this.citaForm.patchValue({ date: fechaStr });
        this.diasSinHoras.delete(fechaStr); // Remover de días sin horas si estaba
      } else {
        // No hay horas disponibles, no seleccionar la fecha y marcar el día
        this.fechaSeleccionada = null;
        this.citaForm.patchValue({ date: '' });
        this.horasDisponibles = [];
        this.diasSinHoras.add(fechaStr); // Marcar como día sin horas
      }
    });
  }

  cargarHorasDisponibles(dia: Date, callback?: (tieneHoras: boolean) => void): void {
    const stylistId = this.citaForm.get('stylistId')?.value;
    if (!stylistId) {
      this.horasDisponibles = [];
      if (callback) callback(false);
      return;
    }

    this.cargandoHoras = true;
    const fechaStr = this.formatearFecha(dia);
    
    // Obtener el día de la semana (0=domingo, 1=lunes, etc. en JavaScript)
    const jsDay = dia.getDay();
    // Mapear a nombres de días: Domingo=0→SUNDAY, Lunes=1→MONDAY, etc.
    const dayNamesMap: { [key: number]: string } = {
      0: 'SUNDAY',    // Domingo
      1: 'MONDAY',    // Lunes
      2: 'TUESDAY',   // Martes
      3: 'WEDNESDAY', // Miércoles
      4: 'THURSDAY',  // Jueves
      5: 'FRIDAY',    // Viernes
      6: 'SATURDAY'   // Sábado
    };
    const dayName = dayNamesMap[jsDay];

    // Cargar disponibilidades del estilista para este día
    const stylistIdNum = Number(stylistId);
    
    const disponibilidadDelDia = this.disponibilidades.find((disp: Availability) => {
      const dispStylistId = Number(disp.stylistId || 0);
      // Normalizar el enum - puede venir como string o como objeto, convertir a mayúsculas
      let dispDayOfWeek: string = '';
      if (disp.dayOfWeek) {
        if (typeof disp.dayOfWeek === 'object' && disp.dayOfWeek !== null) {
          dispDayOfWeek = String(disp.dayOfWeek).toUpperCase();
        } else {
          dispDayOfWeek = String(disp.dayOfWeek).toUpperCase();
        }
      }
      return dispStylistId === stylistIdNum && dispDayOfWeek === dayName;
    });

    if (!disponibilidadDelDia) {
      this.horasDisponibles = [];
      this.cargandoHoras = false;
      if (callback) {
        callback(false);
      }
      return;
    }

    // Cargar citas del día para ver qué horas están ocupadas
    this.apiService.getCitas({ estilistaId: stylistId, fecha: fechaStr }).subscribe({
      next: (citasData: Appointment[] | { content: Appointment[] }) => {
        const citas = Array.isArray(citasData) ? citasData : (citasData.content || []);
        this.citasDelDia = citas.filter((cita: Appointment) => {
          const citaFecha = new Date(cita.date).toISOString().split('T')[0];
          return cita.status !== 'CANCELADA' && citaFecha === fechaStr;
        });
        this.generarHorasDisponibles(disponibilidadDelDia);
        this.cargandoHoras = false;
        if (callback) {
          callback(this.horasDisponibles.length > 0);
        }
      },
      error: (err) => {
        console.error('Error al cargar citas del día:', err);
        // Continuar con la generación de horas incluso si falla la carga de citas
        // (asumiendo que no hay citas existentes)
        this.citasDelDia = [];
        this.generarHorasDisponibles(disponibilidadDelDia);
        this.cargandoHoras = false;
        if (callback) {
          callback(this.horasDisponibles.length > 0);
        }
      }
    });
  }

  generarHorasDisponibles(disponibilidad: Availability | any): void {
    const horas: string[] = [];
    // El backend devuelve startTime y endTime según el tipo Availability
    const startTime = disponibilidad.startTime;
    const endTime = disponibilidad.endTime;
    
    if (!startTime || !endTime) {
      this.horasDisponibles = [];
      return;
    }
    
    const horaInicio = this.parseTime(startTime);
    const horaFin = this.parseTime(endTime);
    
    // Generar slots de 30 minutos
    let horaActual = horaInicio;
    
    while (horaActual < horaFin) {
      const horaStr = this.formatTime(horaActual);
      const horaFinSlot = this.formatTime(horaActual + 30);
      
      // Verificar si hay conflicto con citas existentes (excluyendo citas canceladas)
      const tieneConflicto = this.citasDelDia.some((cita: Appointment) => {
        // Ignorar citas canceladas
        if (cita.status === 'CANCELADA' || cita.estado === 'CANCELADA') {
          return false;
        }
        
        const citaStartTime = cita.startTime;
        const citaEndTime = cita.endTime;
        if (!citaStartTime || !citaEndTime) return false;
        
        const citaInicio = this.parseTime(citaStartTime);
        const citaFin = this.parseTime(citaEndTime);
        
        // Verificar solapamiento: si la hora del slot se solapa con alguna cita existente
        // Un slot de 30 minutos desde horaActual hasta horaActual+30 se solapa si:
        // - Comienza dentro de la cita (horaActual >= citaInicio && horaActual < citaFin)
        // - Termina dentro de la cita (horaActual + 30 > citaInicio && horaActual + 30 <= citaFin)
        // - Envuelve completamente la cita (horaActual < citaInicio && horaActual + 30 > citaFin)
        const slotInicio = horaActual;
        const slotFin = horaActual + 30;
        
        return (slotInicio >= citaInicio && slotInicio < citaFin) ||
               (slotFin > citaInicio && slotFin <= citaFin) ||
               (slotInicio < citaInicio && slotFin > citaFin);
      });
      
      if (!tieneConflicto) {
        horas.push(horaStr);
      }
      
      horaActual += 30; // Incrementar 30 minutos
    }
    
    this.horasDisponibles = horas;
  }

  parseTime(timeStr: string | any): number {
    if (!timeStr) {
      return 0;
    }
    
    // Si es un objeto LocalTime, extraer horas y minutos
    if (typeof timeStr === 'object' && timeStr !== null) {
      const hours = timeStr.hour !== undefined ? timeStr.hour : (timeStr.hours || 0);
      const minutes = timeStr.minute !== undefined ? timeStr.minute : (timeStr.minutes || 0);
      return hours * 60 + minutes;
    }
    
    // Si es string, manejar formato HH:mm o HH:mm:ss
    const str = String(timeStr);
    const parts = str.split(':');
    if (parts.length < 2) {
      return 0;
    }
    const hours = parseInt(parts[0], 10) || 0;
    const minutes = parseInt(parts[1], 10) || 0;
    return hours * 60 + minutes; // Retorna minutos desde medianoche
  }

  formatTime(minutes: number): string {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return `${String(hours).padStart(2, '0')}:${String(mins).padStart(2, '0')}`;
  }

  seleccionarHora(hora: string): void {
    const stylistId = this.citaForm.get('stylistId')?.value;
    if (!stylistId || !this.fechaSeleccionada) return;
    
    // Verificar que la hora todavía está disponible (por si acaso se reservó mientras el usuario seleccionaba)
    if (!this.horasDisponibles.includes(hora)) {
      this.errorMessage = 'Esta hora ya está ocupada. Por favor, selecciona otra hora.';
      setTimeout(() => this.errorMessage = '', 3000);
      return;
    }
    
    // Verificación adicional: comprobar si la hora está ocupada en citasDelDia
    const minutosHora = this.parseTime(hora);
    const tieneConflicto = this.citasDelDia.some((cita: Appointment) => {
      if (cita.status === 'CANCELADA' || cita.estado === 'CANCELADA') {
        return false;
      }
      const citaStartTime = cita.startTime;
      const citaEndTime = cita.endTime;
      if (!citaStartTime || !citaEndTime) return false;
      
      const citaInicio = this.parseTime(citaStartTime);
      const citaFin = this.parseTime(citaEndTime);
      
      // Verificar si la hora seleccionada se solapa con alguna cita existente
      return minutosHora >= citaInicio && minutosHora < citaFin;
    });
    
    if (tieneConflicto) {
      this.errorMessage = 'Esta hora ya está ocupada. Por favor, selecciona otra hora.';
      setTimeout(() => this.errorMessage = '', 3000);
      return;
    }

    // Calcular la hora de fin basada en los servicios seleccionados
    const serviciosSeleccionados = this.servicios.filter(s => 
      this.serviciosSeleccionados.includes(s.serviceId)
    );
    
    const duracionTotal = serviciosSeleccionados.reduce((sum, s) => sum + (s.duration || 0), 0);
    const minutosInicio = this.parseTime(hora);
    const minutosFin = minutosInicio + (duracionTotal || 60); // Default 60 minutos si no hay servicios
    
    const horaFin = this.formatTime(minutosFin);
    
    this.citaForm.patchValue({
      startTime: hora,
      endTime: horaFin
    });
  }

  getHoraSeleccionada(): string | null {
    return this.citaForm.get('startTime')?.value || null;
  }

  diaSinHoras(dia: Date | null): boolean {
    if (!dia) return false;
    const fechaStr = this.formatearFecha(dia);
    return this.diasSinHoras.has(fechaStr);
  }

  isFechaSeleccionada(dia: Date | null): boolean {
    if (!dia) return false;
    const fechaForm = this.citaForm.get('date')?.value;
    if (!fechaForm) return false;
    const fechaStr = this.formatearFecha(dia);
    return fechaStr === fechaForm;
  }

  getCurrentMonthValue(): string {
    return `${this.anio}-${String(this.mes + 1).padStart(2, '0')}`;
  }

  cambiarMesDesdeInput(event: Event): void {
    const target = event.target as HTMLInputElement;
    const value = target?.value;
    if (value) {
      const [year, month] = value.split('-').map(Number);
      this.anio = year;
      this.mes = month - 1;
      this.generarCalendario();
    }
  }
}





