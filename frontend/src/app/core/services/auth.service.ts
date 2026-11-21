import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, finalize, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CHAVE_CARRINHO, HEADER_CARRINHO } from './carrinho.service';

export interface UsuarioAutenticado {
  id: number;
  nome: string;
  email: string;
  perfis: string[];
}

export interface LoginResponse {
  token: string;
  expiraEm: string;
  usuario: UsuarioAutenticado;
  perfis: string[];
  /** Perfil com que a sessão entrou; null enquanto quem tem os dois perfis não escolhe (#20). */
  perfilAtivo: 'CLIENTE' | 'ADMINISTRATIVO' | null;
  escolherPerfil: boolean;
  /** 2FA ligado (#45): token nulo até POST /auth/login/2fa com o tokenTemporario. */
  precisa2fa?: boolean;
  tokenTemporario?: string | null;
}

/** Ids do enum PerfilAtivo (GET /perfis-ativos). */
export const PERFIL_ATIVO = { CLIENTE: 1, ADMINISTRATIVO: 2 } as const;

/** GET /conta/sessoes. */
export interface SessaoUsuario {
  id: number;
  perfilAtivo: { id: number; nome: string } | null;
  dataCadastro: string;
  expiraEm: string;
  atual: boolean;
}

export const CHAVE_TOKEN = 'aeria-auth-token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/auth`;

  login(email: string, senha: string, manterConectado: boolean): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(
        `${this.baseUrl}/login`,
        { email, senha },
        { headers: this.headersCarrinho() },
      )
      .pipe(tap((resposta) => this.guardar(resposta, manterConectado)));
  }

  /** Segunda etapa do login com 2FA (#45). */
  login2fa(
    tokenTemporario: string,
    codigo: string,
    manterConectado: boolean,
  ): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${this.baseUrl}/login/2fa`, { tokenTemporario, codigo })
      .pipe(tap((resposta) => this.guardar(resposta, manterConectado)));
  }

  /** Login com o ID token do Google Identity Services (#47). */
  google(idToken: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(
        `${this.baseUrl}/google`,
        { idToken },
        { headers: this.headersCarrinho() },
      )
      .pipe(tap((resposta) => this.guardar(resposta, false)));
  }

  // O carrinho anônimo vai junto: o backend vincula no login (RF17)
  private headersCarrinho(): HttpHeaders {
    let t: string | null = null;
    try {
      t = localStorage.getItem(CHAVE_CARRINHO);
    } catch {
      t = null;
    }
    return t ? new HttpHeaders({ [HEADER_CARRINHO]: t }) : new HttpHeaders();
  }

  private guardar(resposta: LoginResponse, manterConectado: boolean): void {
    if (resposta.token) this.salvarToken(resposta.token, manterConectado);
  }

  /** Troca o token parcial pelo do perfil escolhido, no mesmo armazenamento do login. */
  escolherPerfil(idPerfilAtivo: number): Observable<LoginResponse> {
    const manter = localStorage.getItem(CHAVE_TOKEN) !== null;
    return this.http
      .post<LoginResponse>(`${this.baseUrl}/perfil-ativo`, { idPerfilAtivo })
      .pipe(tap((resposta) => this.salvarToken(resposta.token, manter)));
  }

  sessoes(): Observable<SessaoUsuario[]> {
    return this.http.get<SessaoUsuario[]>(`${environment.apiUrl}/conta/sessoes`);
  }

  revogarSessao(id: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/conta/sessoes/${id}`);
  }

  logout(): Observable<void> {
    return this.http
      .post<void>(`${this.baseUrl}/logout`, {})
      .pipe(finalize(() => this.limparSessao()));
  }

  token(): string | null {
    for (const armazenamento of [localStorage, sessionStorage]) {
      const token = armazenamento.getItem(CHAVE_TOKEN);
      if (!token) continue;
      if (this.tokenAindaValido(token)) return token;
      armazenamento.removeItem(CHAVE_TOKEN);
    }
    return null;
  }

  autenticado(): boolean {
    return this.token() !== null;
  }

  private salvarToken(token: string, manterConectado: boolean): void {
    this.limparSessao();
    const armazenamento = manterConectado ? localStorage : sessionStorage;
    armazenamento.setItem(CHAVE_TOKEN, token);
  }

  limparSessao(): void {
    localStorage.removeItem(CHAVE_TOKEN);
    sessionStorage.removeItem(CHAVE_TOKEN);
  }

  private tokenAindaValido(token: string): boolean {
    try {
      const parte = token.split('.')[1];
      if (!parte) return false;
      const base64 = parte.replace(/-/g, '+').replace(/_/g, '/');
      const claims = JSON.parse(atob(base64.padEnd(Math.ceil(base64.length / 4) * 4, '='))) as {
        exp?: unknown;
      };
      return typeof claims.exp === 'number' && claims.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }
}
