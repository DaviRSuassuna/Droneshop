import { inject } from '@angular/core';
import { CanActivateChildFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const adminAuthGuard: CanActivateChildFn = (_route, state) => {
  if (inject(AuthService).autenticado()) return true;

  return inject(Router).createUrlTree(['/entrar'], {
    queryParams: { returnUrl: state.url },
  });
};
