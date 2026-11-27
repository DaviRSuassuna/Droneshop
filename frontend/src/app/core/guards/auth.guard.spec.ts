import { TestBed } from '@angular/core/testing';
import { Component } from '@angular/core';
import { Router, provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { Papel } from '../models/auth.model';
import { AuthService } from '../services/auth.service';
import { authGuard } from './auth.guard';
import { papelGuard } from './papel.guard';

@Component({ template: '' })
class Pagina {}

describe('authGuard e papelGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([
          { path: 'entrar', component: Pagina },
          { path: 'acesso-negado', component: Pagina },
          { path: 'privada', component: Pagina, canActivate: [authGuard] },
          { path: 'admin', component: Pagina, canActivate: [papelGuard(['ADMINISTRATIVO'])] },
          { path: 'conta', component: Pagina, canActivate: [papelGuard(['CLIENTE'])] },
        ]),
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
  });

  afterEach(() => TestBed.inject(HttpTestingController).verify());

  function autenticar(papel: Papel) {
    TestBed.inject(AuthService).renovar().subscribe();
    TestBed.inject(HttpTestingController)
      .expectOne(`${environment.apiUrl}/auth/renovar`)
      .flush({ accessToken: 'acesso', expiraEm: 300, papeis: [papel] });
  }

  it.each(['/privada', '/admin'])(
    'redireciona de %s para login e preserva a volta',
    async (url) => {
      const harness = await RouterTestingHarness.create();
      await harness.navigateByUrl(`${url}?filtro=ativo`);
      const router = TestBed.inject(Router);
      expect(router.url.split('?')[0]).toBe('/entrar');
      expect(router.parseUrl(router.url).queryParamMap.get('volta')).toBe(`${url}?filtro=ativo`);
    },
  );

  it.each<Papel>(['ADMINISTRATIVO', 'ADMIN'])('autoriza %s no painel', async (papel) => {
    autenticar(papel);
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/admin');
    expect(TestBed.inject(Router).url).toBe('/admin');
  });

  it('autoriza cliente na conta e em rota com autenticação sem papel', async () => {
    autenticar('CLIENTE');
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/conta');
    expect(TestBed.inject(Router).url).toBe('/conta');
    await harness.navigateByUrl('/privada');
    expect(TestBed.inject(Router).url).toBe('/privada');
  });

  it.each<[Papel, string]>([
    ['CLIENTE', '/admin'],
    ['ADMIN', '/conta'],
  ])('nega o papel %s na rota %s', async (papel, url) => {
    autenticar(papel);
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl(url);
    expect(TestBed.inject(Router).url).toBe('/acesso-negado');
  });
});
