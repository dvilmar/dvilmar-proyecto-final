import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { AuthService } from './auth.service';

// Importaciones para WebSocket
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const API_URL = 'http://localhost:8080/api';
const WS_URL = 'http://localhost:8080/ws';

export interface Notification {
  notificationId: number;
  userId: number;
  title: string;
  message: string;
  type: string;
  read: boolean;
  relatedAppointmentId?: number;
  createdDate: string;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private stompClient: Client | null = null;
  private notificationsSubject = new BehaviorSubject<Notification[]>([]);
  public notifications$ = this.notificationsSubject.asObservable();
  private unreadCountSubject = new BehaviorSubject<number>(0);
  public unreadCount$ = this.unreadCountSubject.asObservable();

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {
    this.connectWebSocket();
  }

  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    });
  }

  connectWebSocket(): void {
    if (!this.authService.isAuthenticated()) {
      return;
    }

    const socket = new SockJS(WS_URL);
    this.stompClient = new Client({
      webSocketFactory: () => {
        return socket as any;
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: (str) => {
      }
    });

    this.stompClient.onConnect = (frame) => {
      const user = this.authService.getCurrentUser();
      if (user && this.stompClient) {
        this.stompClient.subscribe(`/topic/notifications/${user.usuarioId}`, (message) => {
          if (message.body) {
            const notification: Notification = JSON.parse(message.body);
            this.addNotification(notification);
            this.refreshUnreadCount();
          }
        });
      }
    };

    this.stompClient.onStompError = (frame) => {
      console.error('STOMP error: ' + frame.headers['message'], frame);
    };

    this.stompClient.activate();
  }

  disconnectWebSocket(): void {
    if (this.stompClient) {
      this.stompClient.deactivate();
    }
  }

  private addNotification(notification: Notification): void {
    const current = this.notificationsSubject.value;
    this.notificationsSubject.next([notification, ...current]);
  }

  getNotifications(page: number = 0, size: number = 10): Observable<any> {
    return this.http.get(`${API_URL}/notificaciones?page=${page}&size=${size}`, {
      headers: this.getHeaders()
    });
  }

  getUnreadNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${API_URL}/notificaciones/unread`, {
      headers: this.getHeaders()
    });
  }

  getUnreadCount(): Observable<number> {
    return this.http.get<number>(`${API_URL}/notificaciones/unread/count`, {
      headers: this.getHeaders()
    });
  }

  refreshUnreadCount(): void {
    this.getUnreadCount().subscribe({
      next: (count) => {
        this.unreadCountSubject.next(count);
      },
      error: (err) => {
        console.error('Error getting unread count:', err);
      }
    });
  }

  markAsRead(notificationId: number): Observable<Notification> {
    return this.http.patch<Notification>(`${API_URL}/notificaciones/${notificationId}/read`, {}, {
      headers: this.getHeaders()
    });
  }

  markAllAsRead(): Observable<void> {
    return this.http.patch<void>(`${API_URL}/notificaciones/read-all`, {}, {
      headers: this.getHeaders()
    });
  }

  deleteNotification(notificationId: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/notificaciones/${notificationId}`, {
      headers: this.getHeaders()
    });
  }
}





