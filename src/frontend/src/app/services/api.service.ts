import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

const API_URL = 'http://localhost:8080/api';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    });
  }

  // Usuarios
  getCurrentUser(): Observable<any> {
    return this.http.get(`${API_URL}/usuarios/me`, { headers: this.getHeaders() });
  }

  updateProfile(data: any): Observable<any> {
    return this.http.put(`${API_URL}/usuarios/me`, data, { headers: this.getHeaders() });
  }

  // Citas
  getCitas(filters?: any, page?: number, size?: number, paginated?: boolean): Observable<any> {
    let url = `${API_URL}/citas`;
    const params = new URLSearchParams();
    if (filters) {
      if (filters.clienteId) params.append('clienteId', filters.clienteId);
      if (filters.estilistaId) params.append('estilistaId', filters.estilistaId);
      if (filters.fecha) params.append('fecha', filters.fecha);
      if (filters.nombreCliente) params.append('nombreCliente', filters.nombreCliente);
      if (filters.nombreEstilista) params.append('nombreEstilista', filters.nombreEstilista);
      if (filters.nombreServicio) params.append('nombreServicio', filters.nombreServicio);
      if (filters.estado) params.append('estado', filters.estado);
    }
    if (paginated) {
      params.append('paginated', 'true');
      params.append('page', (page || 0).toString());
      params.append('size', (size || 10).toString());
    }
    if (params.toString()) {
      url += '?' + params.toString();
    }
    // Usar endpoint público si no hay token (para reservas públicas)
    const token = this.authService.getToken();
    if (token) {
      return this.http.get(url, { headers: this.getHeaders() });
    } else {
      return this.http.get(url);
    }
  }

  getCitaById(id: number): Observable<any> {
    return this.http.get(`${API_URL}/citas/${id}`, { headers: this.getHeaders() });
  }

  createCita(data: any, isPublic: boolean = false): Observable<any> {
    if (isPublic) {
      // Usar endpoint público si no hay autenticación
      return this.http.post(`${API_URL}/citas/public`, data);
    } else {
      // Usar endpoint autenticado
      return this.http.post(`${API_URL}/citas`, data, { headers: this.getHeaders() });
    }
  }

  updateCita(id: number, data: any): Observable<any> {
    return this.http.patch(`${API_URL}/citas/${id}`, data, { headers: this.getHeaders() });
  }

  deleteCita(id: number): Observable<any> {
    return this.http.delete(`${API_URL}/citas/${id}`, { headers: this.getHeaders() });
  }

  // Servicios
  getServicios(page?: number, size?: number, paginated?: boolean): Observable<any> {
    let url = `${API_URL}/servicios`;
    const params = new URLSearchParams();
    if (paginated) {
      params.append('paginated', 'true');
      params.append('page', (page || 0).toString());
      params.append('size', (size || 10).toString());
    }
    if (params.toString()) {
      url += '?' + params.toString();
    }
    return this.http.get(url);
  }

  getServicioById(id: number): Observable<any> {
    return this.http.get(`${API_URL}/servicios/${id}`);
  }

  createServicio(data: any): Observable<any> {
    return this.http.post(`${API_URL}/servicios`, data, { headers: this.getHeaders() });
  }

  updateServicio(id: number, data: any): Observable<any> {
    return this.http.put(`${API_URL}/servicios/${id}`, data, { headers: this.getHeaders() });
  }

  deleteServicio(id: number): Observable<any> {
    return this.http.delete(`${API_URL}/servicios/${id}`, { headers: this.getHeaders() });
  }

  // Usuarios (Admin)
  getAllUsuarios(page?: number, size?: number, paginated?: boolean): Observable<any> {
    let url = `${API_URL}/usuarios`;
    const params = new URLSearchParams();
    if (paginated) {
      params.append('paginated', 'true');
      params.append('page', (page || 0).toString());
      params.append('size', (size || 10).toString());
    }
    if (params.toString()) {
      url += '?' + params.toString();
    }
    return this.http.get(url, { headers: this.getHeaders() });
  }

  getUsuarioById(id: number): Observable<any> {
    return this.http.get(`${API_URL}/usuarios/${id}`, { headers: this.getHeaders() });
  }

  updateUsuario(id: number, data: any): Observable<any> {
    return this.http.patch(`${API_URL}/usuarios/${id}`, data, { headers: this.getHeaders() });
  }

  toggleUsuarioActivo(id: number): Observable<any> {
    return this.http.patch(`${API_URL}/usuarios/${id}/activo`, {}, { headers: this.getHeaders() });
  }

  // Estilistas
  getEstilistas(): Observable<any> {
    // Siempre usar el endpoint público para obtener estilistas
    // Si es un admin autenticado y necesita más información, puede usar getAllUsuarios con filtros
    return this.http.get(`${API_URL}/usuarios/public/estilistas`);
  }

  // Servicios de Estilista
  getMisServicios(): Observable<any> {
    return this.http.get(`${API_URL}/estilistas/me/servicios`, { headers: this.getHeaders() });
  }

  getServiciosDeEstilista(stylistId: number): Observable<any> {
    return this.http.get(`${API_URL}/estilistas/${stylistId}/servicios`, { headers: this.getHeaders() });
  }

  asociarServicios(stylistId: number, serviceIds: number[]): Observable<any> {
    return this.http.post(`${API_URL}/estilistas/${stylistId}/servicios`, 
      { serviceIds: serviceIds }, 
      { headers: this.getHeaders() });
  }

  // Disponibilidad
  getDisponibilidades(estilistaId?: number, page?: number, size?: number, paginated?: boolean): Observable<any> {
    let url = `${API_URL}/disponibilidades`;
    const params = new URLSearchParams();
    if (estilistaId) {
      params.append('estilistaId', estilistaId.toString());
    }
    if (paginated) {
      params.append('paginated', 'true');
      params.append('page', (page || 0).toString());
      params.append('size', (size || 10).toString());
    }
    if (params.toString()) {
      url += '?' + params.toString();
    }
    // Usar endpoint público si no hay token (para reservas públicas)
    const token = this.authService.getToken();
    if (token) {
      return this.http.get(url, { headers: this.getHeaders() });
    } else {
      return this.http.get(url);
    }
  }

  createDisponibilidad(data: any): Observable<any> {
    return this.http.post(`${API_URL}/disponibilidades`, data, { headers: this.getHeaders() });
  }

  updateDisponibilidad(id: number, data: any): Observable<any> {
    return this.http.put(`${API_URL}/disponibilidades/${id}`, data, { headers: this.getHeaders() });
  }

  deleteDisponibilidad(id: number): Observable<any> {
    return this.http.delete(`${API_URL}/disponibilidades/${id}`, { headers: this.getHeaders() });
  }

  // Excepciones de horario
  getExcepciones(filters?: any): Observable<any> {
    let url = `${API_URL}/excepciones-horario`;
    if (filters) {
      const params = new URLSearchParams();
      if (filters.estilistaId) params.append('estilistaId', filters.estilistaId);
      if (filters.fecha) params.append('fecha', filters.fecha);
      if (params.toString()) url += '?' + params.toString();
    }
    // Usar endpoint público si no hay token (para reservas públicas)
    const token = this.authService.getToken();
    if (token) {
      return this.http.get(url, { headers: this.getHeaders() });
    } else {
      return this.http.get(url);
    }
  }

  createExcepcion(data: any): Observable<any> {
    return this.http.post(`${API_URL}/excepciones-horario`, data, { headers: this.getHeaders() });
  }

  updateExcepcion(id: number, data: any): Observable<any> {
    return this.http.put(`${API_URL}/excepciones-horario/${id}`, data, { headers: this.getHeaders() });
  }

  deleteExcepcion(id: number): Observable<any> {
    return this.http.delete(`${API_URL}/excepciones-horario/${id}`, { headers: this.getHeaders() });
  }

  // Notificaciones
  getNotifications(page: number = 0, size: number = 10): Observable<any> {
    return this.http.get(`${API_URL}/notificaciones?page=${page}&size=${size}`, { headers: this.getHeaders() });
  }

  getUnreadNotifications(): Observable<any> {
    return this.http.get(`${API_URL}/notificaciones/unread`, { headers: this.getHeaders() });
  }

  getUnreadCount(): Observable<any> {
    return this.http.get(`${API_URL}/notificaciones/unread/count`, { headers: this.getHeaders() });
  }

  markNotificationAsRead(id: number): Observable<any> {
    return this.http.patch(`${API_URL}/notificaciones/${id}/read`, {}, { headers: this.getHeaders() });
  }

  markAllNotificationsAsRead(): Observable<any> {
    return this.http.patch(`${API_URL}/notificaciones/read-all`, {}, { headers: this.getHeaders() });
  }

  deleteNotification(id: number): Observable<any> {
    return this.http.delete(`${API_URL}/notificaciones/${id}`, { headers: this.getHeaders() });
  }
}






