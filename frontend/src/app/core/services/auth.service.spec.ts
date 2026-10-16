import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { AuthService, CHAVE_TOKEN } from './auth.service';

function token(exp: number): string {
  const payload = btoa(JSON.stringify({ exp }))
    .replace(/=/g, '')
    .replace(/\+/g, '-')
    .replace(/\//g, '_');
  return `eyJhbGciOiJSUzI1NiJ9.${payload}.assinatura`;
}

function storage(): Storage {
  const dados = new Map<string, string>();
  return {
    get length() {
      return dados.size;
    },
    clear: () => dados.clear(),
    getItem: (chave) => dados.get(chave) ?? null,
    key: (indice) => Array.from(dados.keys())[indice] ?? null,
    removeItem: (chave) => void dados.delete(chave),
    setItem: (chave, valor) => void dados.set(String(chave), String(valor)),
  };
}

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController | undefined;

  beforeEach(() => {
    Object.defineProperty(globalThis, 'localStorage', { configurable: true, value: storage() });
    Object.defineProperty(globalThis, 'sessionStorage', { configurable: true, value: storage() });
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http?.verify());

  it('autentica e guarda o token na sessão por padrão', () => {
    const accessToken = token(Math.floor(Date.now() / 1000) + 3600);
    service.login('admin@aeria.com.br', 'aeria123', false).subscribe();
    const req = http!.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'admin@aeria.com.br', senha: 'aeria123' });
    req.flush({
      token: accessToken,
      expiraEm: '2026-10-15T06:00:00',
      usuario: { id: 1, nome: 'Administrador', email: 'admin@aeria.com.br', perfis: ['ADMINISTRATIVO'] },
      perfis: ['ADMINISTRATIVO'],
    });

    expect(sessionStorage.getItem(CHAVE_TOKEN)).toBe(accessToken);
    expect(localStorage.getItem(CHAVE_TOKEN)).toBeNull();
    expect(service.autenticado()).toBe(true);
  });

  it('guarda o token em armazenamento persistente quando solicitado', () => {
    const accessToken = token(Math.floor(Date.now() / 1000) + 3600);
    service.login('admin@aeria.com.br', 'aeria123', true).subscribe();
    http!.expectOne(`${environment.apiUrl}/auth/login`).flush({
      token: accessToken,
      expiraEm: '2026-10-15T06:00:00',
      usuario: { id: 1, nome: 'Administrador', email: 'admin@aeria.com.br', perfis: [] },
      perfis: [],
    });

    expect(localStorage.getItem(CHAVE_TOKEN)).toBe(accessToken);
    expect(sessionStorage.getItem(CHAVE_TOKEN)).toBeNull();
  });

  it('remove tokens expirados e tokens malformados', () => {
    localStorage.setItem(CHAVE_TOKEN, token(Math.floor(Date.now() / 1000) - 1));
    expect(service.autenticado()).toBe(false);
    expect(localStorage.getItem(CHAVE_TOKEN)).toBeNull();

    sessionStorage.setItem(CHAVE_TOKEN, 'nao-e-um-jwt');
    expect(service.autenticado()).toBe(false);
    expect(sessionStorage.getItem(CHAVE_TOKEN)).toBeNull();
  });

  it('limpa a sessão ao encerrar a autenticação', () => {
    sessionStorage.setItem(CHAVE_TOKEN, token(Math.floor(Date.now() / 1000) + 3600));
    service.logout().subscribe();
    const req = http!.expectOne(`${environment.apiUrl}/auth/logout`);
    expect(req.request.method).toBe('POST');
    req.flush(null);

    expect(service.autenticado()).toBe(false);
  });
});
