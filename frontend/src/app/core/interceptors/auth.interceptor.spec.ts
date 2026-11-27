import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  const api = environment.apiUrl;
  const login = `${api}/auth/login`;
  const renovar = `${api}/auth/renovar`;
  const eu = `${api}/auth/eu`;
  const perfil = `${api}/conta/perfil`;
  const sessao = { accessToken: 'acesso', expiraEm: 300, papeis: ['CLIENTE'] };
  const naoAutorizado = { status: 401, statusText: 'Unauthorized' };
  let auth: AuthService;
  let client: HttpClient;
  let http: HttpTestingController;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    auth = TestBed.inject(AuthService);
    client = TestBed.inject(HttpClient);
    http = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    vi.spyOn(router, 'url', 'get').mockReturnValue('/conta/perfil?aba=dados');
  });

  afterEach(() => http.verify());

  function autenticar() {
    auth.login('ana@exemplo.com', 'senha-ficticia').subscribe();
    http.expectOne(login).flush(sessao);
  }

  it('anexa Bearer a /auth/eu e não envia o token a outro domínio', () => {
    autenticar();
    auth.eu().subscribe();
    const req = http.expectOne(eu);
    expect(req.request.headers.get('Authorization')).toBe('Bearer acesso');
    req.flush({});

    const externa = 'https://exemplo.com/catalogo';
    client.get(externa).subscribe();
    const reqExterna = http.expectOne(externa);
    expect(reqExterna.request.headers.has('Authorization')).toBe(false);
    reqExterna.flush({});
  });

  it('entrega o desafio OTP ao login sem tentar renovar ou navegar', () => {
    autenticar();
    const falha = vi.fn();
    auth.login('ana@exemplo.com', 'senha-ficticia').subscribe({ error: falha });
    const req = http.expectOne(login);
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({ codigo: 'otp-necessario' }, naoAutorizado);

    http.expectNone(renovar);
    expect(falha.mock.calls[0]?.[0].error.codigo).toBe('otp-necessario');
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('renova uma vez para dois 401 simultâneos e repete as chamadas com o novo Bearer', () => {
    autenticar();
    const sucesso = vi.fn();
    client.get(perfil).subscribe(sucesso);
    auth.eu().subscribe(sucesso);
    http.expectOne(perfil).flush(null, naoAutorizado);
    http.expectOne(eu).flush(null, naoAutorizado);
    const refresh = http.expectOne(renovar);
    expect(refresh.request.headers.has('Authorization')).toBe(false);
    refresh.flush({ ...sessao, accessToken: 'novo' });
    for (const url of [perfil, eu]) {
      const repetida = http.expectOne(url);
      expect(repetida.request.headers.get('Authorization')).toBe('Bearer novo');
      repetida.flush({});
    }
    expect(sucesso).toHaveBeenCalledTimes(2);
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('aproveita uma renovação concluída se um 401 do token antigo chegar depois', () => {
    autenticar();
    client.get(perfil).subscribe();
    auth.eu().subscribe();
    const atrasada = http.expectOne(eu);
    http.expectOne(perfil).flush(null, naoAutorizado);
    http.expectOne(renovar).flush({ ...sessao, accessToken: 'novo' });
    http.expectOne(perfil).flush({});

    atrasada.flush(null, naoAutorizado);
    const repetida = http.expectOne(eu);
    expect(repetida.request.headers.get('Authorization')).toBe('Bearer novo');
    repetida.flush({});
    http.expectNone(renovar);
  });

  it('não repete chamada de uma conta anterior ao entrar em outra', () => {
    autenticar();
    const falha = vi.fn();
    client.get(perfil).subscribe({ error: falha });
    const anterior = http.expectOne(perfil);
    auth.login('bia@exemplo.com', 'senha-ficticia').subscribe();
    http.expectOne(login).flush({ ...sessao, accessToken: 'outra-conta' });

    anterior.flush(null, naoAutorizado);
    http.expectNone(renovar);
    http.expectNone(perfil);
    expect(falha).toHaveBeenCalledOnce();
    expect(auth.token()).toBe('outra-conta');
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('limpa a sessão e redireciona com volta se a renovação falhar', () => {
    autenticar();
    const falha = vi.fn();
    client.get(perfil).subscribe({ error: falha });
    http.expectOne(perfil).flush(null, naoAutorizado);
    http.expectOne(renovar).flush(null, naoAutorizado);
    expect(falha).toHaveBeenCalledOnce();
    expect(auth.token()).toBeNull();
    expect(router.navigate).toHaveBeenCalledWith(['/entrar'], {
      queryParams: { volta: '/conta/perfil?aba=dados' },
    });
  });

  it('não renova de novo se a repetição também receber 401', () => {
    autenticar();
    const falha = vi.fn();
    client.get(perfil).subscribe({ error: falha });
    http.expectOne(perfil).flush(null, naoAutorizado);
    http.expectOne(renovar).flush({ ...sessao, accessToken: 'novo' });
    http.expectOne(perfil).flush(null, naoAutorizado);
    http.expectNone(renovar);
    expect(falha).toHaveBeenCalledOnce();
    expect(auth.token()).toBeNull();
    expect(router.navigate).toHaveBeenCalledWith(['/entrar'], {
      queryParams: { volta: '/conta/perfil?aba=dados' },
    });
  });

  it('exibe acesso negado no 403 e preserva a sessão', () => {
    autenticar();
    const falha = vi.fn();
    client.get(perfil).subscribe({ error: falha });
    http.expectOne(perfil).flush(null, { status: 403, statusText: 'Forbidden' });
    http.expectNone(renovar);
    expect(falha).toHaveBeenCalledOnce();
    expect(auth.token()).toBe('acesso');
    expect(router.navigate).toHaveBeenCalledWith(['/acesso-negado']);
  });
});
