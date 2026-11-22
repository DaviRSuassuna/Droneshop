import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { vi } from 'vitest';
import { environment } from '../../../environments/environment';
import { CHAVE_TOKEN } from '../services/auth.service';
import { authInterceptor } from './auth.interceptor';

function token(): string {
  const payload = btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + 3600 }))
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

describe('authInterceptor', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    Object.defineProperty(globalThis, 'localStorage', { configurable: true, value: storage() });
    Object.defineProperty(globalThis, 'sessionStorage', { configurable: true, value: storage() });
    localStorage.setItem(CHAVE_TOKEN, token());
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('envia o bearer token somente à API configurada', () => {
    const client = TestBed.inject(HttpClient);
    client.get(`${environment.apiUrl}/marcas`).subscribe();
    client.get('https://servico-externo.example/dados').subscribe();

    const apiRequest = http.expectOne(`${environment.apiUrl}/marcas`);
    expect(apiRequest.request.headers.get('Authorization')).toBe(`Bearer ${localStorage.getItem(CHAVE_TOKEN)}`);
    apiRequest.flush({});

    const externa = http.expectOne('https://servico-externo.example/dados');
    expect(externa.request.headers.has('Authorization')).toBe(false);
    externa.flush({});
  });

  it('não envia token para o endpoint público de login', () => {
    const client = TestBed.inject(HttpClient);
    client.post(`${environment.apiUrl}/auth/login`, {}).subscribe();
    const req = http.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('limpa a sessão e redireciona ao login quando a API responde 401 na área protegida', () => {
    const router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    vi.spyOn(router, 'url', 'get').mockReturnValue('/admin/marcas');

    TestBed.inject(HttpClient).get(`${environment.apiUrl}/marcas`).subscribe({
      error: () => {},
    });
    http.expectOne(`${environment.apiUrl}/marcas`).flush(
      { detail: 'Não autenticado.' },
      { status: 401, statusText: 'Unauthorized' },
    );

    expect(localStorage.getItem(CHAVE_TOKEN)).toBeNull();
    expect(router.navigate).toHaveBeenCalledWith(['/entrar'], {
      queryParams: { returnUrl: '/admin/marcas' },
    });
  });

  it('na loja pública, com a sessão vencida, refaz a chamada sem o token e não redireciona', () => {
    const router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    vi.spyOn(router, 'url', 'get').mockReturnValue('/drones');
    let resposta: unknown;

    TestBed.inject(HttpClient).get(`${environment.apiUrl}/carrinho`).subscribe((r) => (resposta = r));
    http.expectOne(`${environment.apiUrl}/carrinho`).flush({}, { status: 401, statusText: 'Unauthorized' });
    const repetida = http.expectOne(`${environment.apiUrl}/carrinho`);
    expect(repetida.request.headers.has('Authorization')).toBe(false);
    repetida.flush({ itens: [] });

    expect(resposta).toEqual({ itens: [] });
    expect(localStorage.getItem(CHAVE_TOKEN)).toBeNull();
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
