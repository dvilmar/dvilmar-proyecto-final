import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService, User } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';
import { ThemeService, Theme } from '../../services/theme.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit, OnDestroy {
  currentUser: User | null = null;
  unreadCount = 0;
  currentTheme: Theme = 'light';
  logoImage: string = 'assets/logo-claro.webp';
  private subscriptions: Subscription[] = [];

  constructor(
    private authService: AuthService,
    private router: Router,
    private notificationService: NotificationService,
    private themeService: ThemeService
  ) {}

  ngOnInit(): void {
    // Cargar el usuario actual inmediatamente al inicializar
    this.currentUser = this.authService.getCurrentUser();
    console.log('Navbar - Usuario cargado en ngOnInit:', this.currentUser);
    if (this.currentUser) {
      console.log('Navbar - Rol del usuario:', this.currentUser.rol);
      this.notificationService.refreshUnreadCount();
    }
    
    // Suscribirse a cambios futuros
    const userSub = this.authService.currentUser$.subscribe(user => {
      console.log('Navbar - Usuario actualizado en subscription:', user);
      this.currentUser = user;
      if (user) {
        console.log('Navbar - Rol del usuario actualizado:', user.rol);
        this.notificationService.refreshUnreadCount();
      }
    });
    this.subscriptions.push(userSub);

    const countSub = this.notificationService.unreadCount$.subscribe(count => {
      this.unreadCount = count;
    });
    this.subscriptions.push(countSub);

    // Inicializar tema
    this.currentTheme = this.themeService.getCurrentTheme();
    this.updateLogoImage();
    const themeSub = this.themeService.theme$.subscribe((theme: Theme) => {
      this.currentTheme = theme;
      this.updateLogoImage();
    });
    this.subscriptions.push(themeSub);
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  private updateLogoImage(): void {
    if (this.currentTheme === 'dark') {
      this.logoImage = 'assets/logo-oscuro.png';
    } else {
      this.logoImage = 'assets/logo-claro.webp';
    }
  }

  toggleTheme(): void {
    this.themeService.toggleTheme();
  }

  logout(): void {
    this.authService.logout();
  }
}






