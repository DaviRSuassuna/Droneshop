import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import {
  Observable,
  catchError,
  defer,
  finalize,
  of,
  shareReplay,
  switchMap,
  tap,
  throwError,
} from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginResponse, Papel, UsuarioAutenticado } from '../models/auth.model';
import { Cliente, ContaDTO } from '../models/cliente.model';
import { CHAVE_CARRINHO, HEADER_CARRINHO } from './carrinho.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/auth`;
  private readonly credenciais = { withCredentials: true };
  private readonly sessao = signal<LoginResponse | null>(null);
  private renovacao?: Observable<LoginResponse>;
  private versao = 0;
  private saiu = false;

  constructor() {
    try {
      localStorage.removeItem('aeria-auth-token');
      sessionStorage.removeItem('aeria-auth-token');
    } catch {
      // O navegador pode bloquear o armazenamento.
    }
  }

  login(email: string, senha: string, codigoOtp?: string): Observable<LoginResponse> {
    return defer(() => {
      const versao = ++this.versao;
      return this.http
        .post<LoginResponse>(
          `${this.baseUrl}/login`,
          { email, senha, ...(codigoOtp === undefined ? {} : { codigoOtp }) },
          { ...this.credenciais, headers: this.headersCarrinho() },
        )
        .pipe(
          tap((resposta) => {
            if (this.versao !== versao) return;
            this.saiu = false;
            this.sessao.set(resposta);
          }),
        );
    });
  }

  cadastro(conta: ContaDTO): Observable<Cliente> {
    return this.http.post<Cliente>(`${this.baseUrl}/cadastro`, conta, this.credenciais);
  }

  eu(): Observable<UsuarioAutenticado> {
    return this.http.get<UsuarioAutenticado>(`${this.baseUrl}/eu`, this.credenciais);
  }

  renovar(): Observable<LoginResponse> {
    return defer(() => {
      if (this.saiu) return throwError(() => new Error('A sessão foi encerrada.'));
      if (this.renovacao) return this.renovacao;
      const versao = this.versao;
      this.renovacao = this.http
        .post<LoginResponse | null>(`${this.baseUrl}/renovar`, null, {
          ...this.credenciais,
          timeout: 10000,
        })
        .pipe(
          switchMap((resposta) =>
            resposta ? of(resposta) : throwError(() => new Error('Sem sessão para renovar.')),
          ),
          tap((resposta) => {
            if (this.versao === versao) this.sessao.set(resposta);
          }),
          finalize(() => (this.renovacao = undefined)),
          shareReplay(1),
        );
      return this.renovacao;
    });
  }

  recuperarSenha(email: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/recuperar-senha`, { email }, this.credenciais);
  }

  sair(): Observable<void> {
    return defer(() => {
      this.saiu = true;
      this.limparSessao();
      // Aguarda a troca do cookie em andamento para revogar o refresh mais recente.
      const pendente: Observable<LoginResponse | null> = this.renovacao ?? of(null);
      return pendente.pipe(
        catchError(() => of(null)),
        switchMap(() => this.http.post<void>(`${this.baseUrl}/sair`, null, this.credenciais)),
      );
    });
  }

  versaoSessao(): number {
    return this.versao;
  }

  token(): string | null {
    return this.sessao()?.accessToken ?? null;
  }

  autenticado(): boolean {
    return this.token() !== null;
  }

  temPapel(papel: Papel): boolean {
    const papeis = this.sessao()?.papeis;
    return (
      !!papeis &&
      (papeis.includes(papel) || (papel === 'ADMINISTRATIVO' && papeis.includes('ADMIN')))
    );
  }

  limparSessao(): void {
    this.versao++;
    this.sessao.set(null);
  }

  private headersCarrinho(): HttpHeaders {
    let token: string | null = null;
    try {
      token = localStorage.getItem(CHAVE_CARRINHO);
    } catch {
      // O carrinho anônimo depende do armazenamento do navegador.
    }
    return token ? new HttpHeaders({ [HEADER_CARRINHO]: token }) : new HttpHeaders();
  }
}
