import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/** Área "Minha conta": sem sessão vai para /entrar e volta depois do login. */
export const clienteAuthGuard: CanActivateFn = (_route, state) => {
  if (inject(AuthService).autenticado()) return true;

  return inject(Router).createUrlTree(['/entrar'], {
    queryParams: { returnUrl: state.url },
  });
};
