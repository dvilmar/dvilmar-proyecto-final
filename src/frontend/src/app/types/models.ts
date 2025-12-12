// Tipos e interfaces compartidas para toda la aplicación

export interface ServiceOffer {
  serviceId: number;
  name: string;
  description?: string;
  duration: number;
  unitPrice: number;
}

export interface Stylist {
  usuarioId: number;
  nombre: string;
  username?: string;
  email?: string;
  rol: 'ESTILISTA' | 'CLIENTE' | 'ADMINISTRADOR';
  activo: boolean;
}

export interface Client {
  usuarioId: number;
  nombre: string;
  email?: string;
  phone?: string;
  rol: 'CLIENTE';
  activo: boolean;
}

export interface Availability {
  availabilityId?: number;
  stylistId: number;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
}

export interface ScheduleException {
  exceptionId?: number;
  stylistId?: number;
  date: string;
  startTime?: string;
  endTime?: string;
  type?: 'DISPONIBLE' | 'NO_DISPONIBLE';
  reason?: string;
  stylistName?: string;
}

export interface Appointment {
  appointmentId?: number;
  date: string;
  startTime: string;
  endTime: string;
  status: string;
  estado?: string;
  clientId?: number;
  stylistId?: number;
  clientName?: string;
  stylistName?: string;
  services?: ServiceOffer[];
  clientPhone?: string;
}

export interface AppointmentFilters {
  clienteId?: number;
  estilistaId?: number;
  fecha?: string;
  nombreCliente?: string;
  nombreEstilista?: string;
  nombreServicio?: string;
  estado?: string;
}

