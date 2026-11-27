import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (_rota, estado) => {
  return (
    inject(AuthService).autenticado() ||
    inject(Router).createUrlTree(['/entrar'], {
      queryParams: { volta: estado.url },
    })
  );
};
