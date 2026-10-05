import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

const API_URL = 'http://localhost:8080/api';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  constructor(private http: HttpClient) {}

  // Usuarios
  getCurrentUser(): Observable<any> {
    return this.http.get(`${API_URL}/usuarios/me`);
  }

  updateProfile(data: any): Observable<any> {
    return this.http.put(`${API_URL}/usuarios/me`, data);
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
    return this.http.get(url);
  }

  getCitaById(id: number): Observable<any> {
    return this.http.get(`${API_URL}/citas/${id}`);
  }

  createCita(data: any, isPublic: boolean = false): Observable<any> {
    if (isPublic) {
      return this.http.post(`${API_URL}/citas/public`, data);
    } else {
      return this.http.post(`${API_URL}/citas`, data);
    }
  }

  updateCita(id: number, data: any): Observable<any> {
    return this.http.patch(`${API_URL}/citas/${id}`, data);
  }

  deleteCita(id: number): Observable<any> {
    return this.http.delete(`${API_URL}/citas/${id}`);
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
    return this.http.post(`${API_URL}/servicios`, data);
  }

  updateServicio(id: number, data: any): Observable<any> {
    return this.http.put(`${API_URL}/servicios/${id}`, data);
  }

  deleteServicio(id: number): Observable<any> {
    return this.http.delete(`${API_URL}/servicios/${id}`);
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
    return this.http.get(url);
  }

  getUsuarioById(id: number): Observable<any> {
    return this.http.get(`${API_URL}/usuarios/${id}`);
  }

  crearUsuario(data: any): Observable<any> {
    return this.http.post(`${API_URL}/usuarios`, data);
  }

  updateUsuario(id: number, data: any): Observable<any> {
    return this.http.patch(`${API_URL}/usuarios/${id}`, data);
  }

  toggleUsuarioActivo(id: number): Observable<any> {
    return this.http.patch(`${API_URL}/usuarios/${id}/activo`, {});
  }

  // Estilistas
  getEstilistas(): Observable<any> {
    return this.http.get(`${API_URL}/usuarios/public/estilistas`);
  }

  // Servicios de Estilista
  getMisServicios(): Observable<any> {
    return this.http.get(`${API_URL}/estilistas/me/servicios`);
  }

  getServiciosDeEstilista(stylistId: number): Observable<any> {
    return this.http.get(`${API_URL}/estilistas/${stylistId}/servicios`);
  }

  asociarServicios(stylistId: number, serviceIds: number[]): Observable<any> {
    return this.http.post(`${API_URL}/estilistas/${stylistId}/servicios`, { serviceIds: serviceIds });
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
    return this.http.get(url);
  }

  createDisponibilidad(data: any): Observable<any> {
    return this.http.post(`${API_URL}/disponibilidades`, data);
  }

  updateDisponibilidad(id: number, data: any): Observable<any> {
    return this.http.put(`${API_URL}/disponibilidades/${id}`, data);
  }

  deleteDisponibilidad(id: number): Observable<any> {
    return this.http.delete(`${API_URL}/disponibilidades/${id}`);
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
    return this.http.get(url);
  }

  createExcepcion(data: any): Observable<any> {
    return this.http.post(`${API_URL}/excepciones-horario`, data);
  }

  updateExcepcion(id: number, data: any): Observable<any> {
    return this.http.put(`${API_URL}/excepciones-horario/${id}`, data);
  }

  deleteExcepcion(id: number): Observable<any> {
    return this.http.delete(`${API_URL}/excepciones-horario/${id}`);
  }
}
