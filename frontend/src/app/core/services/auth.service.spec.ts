import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { LoginResponse } from '../models/auth.model';
import { ContaDTO } from '../models/cliente.model';
import { AuthService } from './auth.service';
import { CHAVE_CARRINHO, HEADER_CARRINHO } from './carrinho.service';

describe('AuthService', () => {
  const base = `${environment.apiUrl}/auth`;
  const sessao: LoginResponse = { accessToken: 'acesso', expiraEm: 300, papeis: ['ADMIN'] };
  let auth: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    auth = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    localStorage.removeItem(CHAVE_CARRINHO);
  });

  function requisicao(rota: string, method: 'GET' | 'POST', corpo: unknown) {
    const req = http.expectOne({ url: `${base}/${rota}`, method });
    expect(req.request.withCredentials).toBe(true);
    expect(req.request.body).toEqual(corpo);
    return req;
  }

  it('faz login com o carrinho anônimo e inclui OTP apenas quando informado', () => {
    localStorage.setItem(CHAVE_CARRINHO, 'carrinho');
    const credenciais = { email: 'ana@exemplo.com', senha: 'senha-ficticia' };
    const falha = vi.fn();
    auth.login(credenciais.email, credenciais.senha).subscribe({ error: falha });
    const inicial = requisicao('login', 'POST', credenciais);
    expect(inicial.request.headers.get(HEADER_CARRINHO)).toBe('carrinho');
    inicial.flush({ codigo: 'otp-necessario' }, { status: 401, statusText: 'Unauthorized' });
    expect(falha).toHaveBeenCalledOnce();
    expect(auth.autenticado()).toBe(false);

    auth.login(credenciais.email, credenciais.senha, '012345').subscribe();
    requisicao('login', 'POST', { ...credenciais, codigoOtp: '012345' }).flush(sessao);
    expect(auth.token()).toBe('acesso');
    expect(auth.temPapel('ADMINISTRATIVO')).toBe(true);
    expect(auth.temPapel('CLIENTE')).toBe(false);
    expect(localStorage.getItem('aeria-auth-token')).toBeNull();
    expect(sessionStorage.getItem('aeria-auth-token')).toBeNull();
  });

  it('envia documento e consentimentos ao cadastrar', () => {
    const conta: ContaDTO = {
      nome: 'Ana Lima',
      email: 'ana@exemplo.com',
      senha: 'senha-ficticia',
      idTipoPessoa: 1,
      documento: '529.982.247-25',
      telefone: '',
      consentimentos: [{ tipo: 'TERMOS', versao: '2026-10' }],
    };
    const resposta = vi.fn();
    auth.cadastro(conta).subscribe(resposta);
    requisicao('cadastro', 'POST', conta).flush({ id: 20 });
    expect(resposta).toHaveBeenCalledWith({ id: 20 });
    expect(auth.autenticado()).toBe(false);
  });

  it('consulta o usuário autenticado', () => {
    const usuario = { id: 20, nome: 'Ana', email: 'ana@exemplo.com', papeis: ['ADMIN'] };
    const resposta = vi.fn();
    auth.eu().subscribe(resposta);
    requisicao('eu', 'GET', null).flush(usuario);
    expect(resposta).toHaveBeenCalledWith(usuario);
  });

  it('compartilha uma renovação entre chamadas simultâneas e atualiza o estado', () => {
    const primeira = vi.fn();
    const segunda = vi.fn();
    auth.renovar().subscribe(primeira);
    auth.renovar().subscribe(segunda);
    requisicao('renovar', 'POST', null).flush(sessao);
    expect(primeira).toHaveBeenCalledWith(sessao);
    expect(segunda).toHaveBeenCalledWith(sessao);
    expect(auth.token()).toBe('acesso');

    auth.renovar().subscribe();
    requisicao('renovar', 'POST', null).flush({ ...sessao, accessToken: 'novo' });
    expect(auth.token()).toBe('novo');
  });

  it('solicita recuperação por e-mail', () => {
    auth.recuperarSenha('ana@exemplo.com').subscribe();
    requisicao('recuperar-senha', 'POST', { email: 'ana@exemplo.com' }).flush(null, {
      status: 204,
      statusText: 'No Content',
    });
  });

  it('limpa o estado ao sair e aguarda uma renovação pendente antes de revogar o cookie', () => {
    auth.renovar().subscribe();
    requisicao('renovar', 'POST', null).flush(sessao);
    auth.renovar().subscribe();
    const renovacao = requisicao('renovar', 'POST', null);
    const falha = vi.fn();
    auth.sair().subscribe({ error: falha });

    expect(auth.token()).toBeNull();
    http.expectNone(`${base}/sair`);
    renovacao.flush({ ...sessao, accessToken: 'novo' });
    const sair = requisicao('sair', 'POST', null);
    sair.flush(null, { status: 500, statusText: 'Internal Server Error' });
    expect(falha).toHaveBeenCalledOnce();
    expect(auth.token()).toBeNull();
    expect(auth.temPapel('ADMIN')).toBe(false);

    const renovacaoAposSair = vi.fn();
    auth.renovar().subscribe({ error: renovacaoAposSair });
    http.expectNone(`${base}/renovar`);
    expect(renovacaoAposSair).toHaveBeenCalledOnce();
  });
});
