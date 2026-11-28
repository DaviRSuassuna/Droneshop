import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Papel } from '../models/auth.model';
import { AuthService } from '../services/auth.service';
import { authGuard } from './auth.guard';

export function papelGuard(papeis: readonly Papel[]): CanActivateFn {
  return (rota, estado) => {
    const autenticado = authGuard(rota, estado);
    if (autenticado !== true) return autenticado;

    const auth = inject(AuthService);
    return (
      papeis.some((papel) => auth.temPapel(papel)) ||
      inject(Router).createUrlTree(['/acesso-negado'])
    );
  };
}
