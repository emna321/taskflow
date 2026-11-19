import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { RoleName } from '../models';

/** Verifie que l'utilisateur possede un des roles declares dans data.roles de la route. */
export const roleGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const roles = (route.data?.['roles'] ?? []) as RoleName[];
  if (!auth.isLoggedIn()) return inject(Router).createUrlTree(['/login']);
  return roles.length === 0 || roles.some(r => auth.hasRole(r))
    ? true
    : inject(Router).createUrlTree(['/dashboard']);
};
