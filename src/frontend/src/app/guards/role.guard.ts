import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const requiredRoles = route.data['roles'] as string[];

  if (!authService.isAuthenticated()) {
    router.navigate(['/login']);
    return false;
  }

  const user = authService.getCurrentUser();
  if (user && requiredRoles.includes(user.rol)) {
    return true;
  }

  // Redirigir según el rol del usuario
  if (user) {
    switch (user.rol) {
      case 'ADMINISTRADOR':
        router.navigate(['/admin']);
        break;
      case 'ESTILISTA':
        router.navigate(['/dashboard']);
        break;
      case 'CLIENTE':
      default:
        router.navigate(['/']);
        break;
    }
  } else {
    router.navigate(['/']);
  }
  
  return false;
};







