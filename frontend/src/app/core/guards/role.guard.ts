import { inject } from '@angular/core';
import { CanActivateFn, ActivatedRouteSnapshot, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const auth     = inject(AuthService);
  const router   = inject(Router);
  const required = (route.data['roles'] as string[]) ?? [];
  const user     = auth.getStoredUser();
  if (!user) { router.navigate(['/auth/login']); return false; }
  const hasRole = required.length === 0 || required.some(r => user.roles.includes(r));
  if (!hasRole) { router.navigate(['/']); return false; }
  return true;
};
