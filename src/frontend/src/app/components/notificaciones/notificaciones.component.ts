import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NotificationService, Notification } from '../../services/notification.service';
import { AuthService, User } from '../../services/auth.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-notificaciones',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './notificaciones.component.html'
})
export class NotificacionesComponent implements OnInit, OnDestroy {
  currentUser: User | null = null;
  notifications: Notification[] = [];
  unreadCount = 0;
  loading = false;
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  private subscriptions: Subscription[] = [];

  constructor(
    private notificationService: NotificationService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.loadNotifications();

    // Suscribirse a actualizaciones en tiempo real
    const notifSub = this.notificationService.notifications$.subscribe(notifs => {
      if (notifs.length > 0) {
        this.loadNotifications();
      }
    });
    this.subscriptions.push(notifSub);

    const countSub = this.notificationService.unreadCount$.subscribe(count => {
      this.unreadCount = count;
    });
    this.subscriptions.push(countSub);
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  loadNotifications(): void {
    this.loading = true;
    this.notificationService.getNotifications(this.currentPage, this.pageSize).subscribe({
      next: (response: any) => {
        if (response.content) {
          // Respuesta paginada
          this.notifications = response.content;
          this.totalPages = response.totalPages;
        } else if (Array.isArray(response)) {
          // Respuesta de array simple
          this.notifications = response;
          this.totalPages = 1;
        }
        this.loading = false;
        this.notificationService.refreshUnreadCount();
      },
      error: (err) => {
        console.error('Error loading notifications:', err);
        console.error('Error completo:', JSON.stringify(err, null, 2));
        this.loading = false;
      }
    });
  }

  markAsRead(notification: Notification): void {
    this.notificationService.markAsRead(notification.notificationId).subscribe({
      next: () => {
        notification.read = true;
        this.notificationService.refreshUnreadCount();
      },
      error: (err) => {
        console.error('Error marking notification as read:', err);
      }
    });
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.notifications.forEach(n => n.read = true);
        this.notificationService.refreshUnreadCount();
      },
      error: (err) => {
        console.error('Error marking all as read:', err);
      }
    });
  }

  deleteNotification(notification: Notification): void {
    if (confirm('¿Estás seguro de que quieres eliminar esta notificación?')) {
      this.notificationService.deleteNotification(notification.notificationId).subscribe({
        next: () => {
          this.notifications = this.notifications.filter(n => n.notificationId !== notification.notificationId);
          this.notificationService.refreshUnreadCount();
        },
        error: (err) => {
          console.error('Error deleting notification:', err);
        }
      });
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadNotifications();
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadNotifications();
    }
  }

  getTypeLabel(type: string): string {
    const labels: { [key: string]: string } = {
      'INFO': 'Info',
      'SUCCESS': 'Éxito',
      'WARNING': 'Advertencia',
      'ERROR': 'Error',
      'APPOINTMENT_REMINDER': 'Recordatorio',
      'APPOINTMENT_CANCELLED': 'Cancelada',
      'APPOINTMENT_CONFIRMED': 'Confirmada'
    };
    return labels[type] || type;
  }

  getTypeBadgeClass(type: string): string {
    const classes: { [key: string]: string } = {
      'INFO': 'bg-info',
      'SUCCESS': 'bg-success',
      'WARNING': 'bg-warning',
      'ERROR': 'bg-danger',
      'APPOINTMENT_REMINDER': 'bg-primary',
      'APPOINTMENT_CANCELLED': 'bg-danger',
      'APPOINTMENT_CONFIRMED': 'bg-success'
    };
    return classes[type] || 'bg-secondary';
  }

  formatDate(date: string): string {
    if (!date) return '';
    
    // LocalDateTime viene como "2025-12-12T06:00:00" (sin zona horaria)
    // Parsear manualmente para evitar problemas de zona horaria
    // Si tiene formato ISO con T (YYYY-MM-DDTHH:mm:ss)
    if (date.includes('T') && !date.includes('Z') && !date.includes('+')) {
      // Es un LocalDateTime sin zona horaria, parsearlo como local
      const [datePart, timePart] = date.split('T');
      const [year, month, day] = datePart.split('-').map(Number);
      const timeParts = timePart.split(':');
      const hours = Number(timeParts[0]);
      const minutes = Number(timeParts[1]);
      const seconds = timeParts.length > 2 ? Number(timeParts[2].split('.')[0]) : 0; // Remover milisegundos si existen
      
      // Crear fecha local sin conversión de zona horaria
      const localDate = new Date(year, month - 1, day, hours, minutes, seconds);
      
      return localDate.toLocaleString('es-ES', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
      });
    }
    
    // Si tiene formato ISO con Z o offset, usar el método normal pero sin conversión
    const d = new Date(date);
    // Verificar si la fecha es válida
    if (isNaN(d.getTime())) {
      return date; // Retornar el string original si no se puede parsear
    }
    
    return d.toLocaleString('es-ES', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    });
  }
}





