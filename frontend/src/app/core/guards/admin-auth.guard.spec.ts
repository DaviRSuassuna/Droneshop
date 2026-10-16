import { provideRouter, Router, RouterStateSnapshot, ActivatedRouteSnapshot, UrlTree } from '@angular/router';
import { TestBed } from '@angular/core/testing';
import { AuthService } from '../services/auth.service';
import { adminAuthGuard } from './admin-auth.guard';

describe('adminAuthGuard', () => {
  it('redireciona usuários sem token para /entrar e preserva a rota desejada', () => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: { autenticado: () => false } },
      ],
    });
    const router = TestBed.inject(Router);
    const resultado = TestBed.runInInjectionContext(() =>
      adminAuthGuard({} as ActivatedRouteSnapshot, { url: '/admin/marcas' } as RouterStateSnapshot),
    );

    expect(resultado).toBeInstanceOf(UrlTree);
    if (!(resultado instanceof UrlTree)) throw new Error('A rota deveria redirecionar para o login.');
    expect(router.serializeUrl(resultado)).toContain('/entrar?returnUrl=');
  });

  it('permite a navegação quando há sessão válida', () => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: { autenticado: () => true } },
      ],
    });
    const resultado = TestBed.runInInjectionContext(() =>
      adminAuthGuard({} as ActivatedRouteSnapshot, { url: '/admin/marcas' } as RouterStateSnapshot),
    );

    expect(resultado).toBe(true);
  });
});
