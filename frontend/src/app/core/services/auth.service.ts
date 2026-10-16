import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, finalize, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

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
}

export const CHAVE_TOKEN = 'aeria-auth-token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/auth`;

  login(email: string, senha: string, manterConectado: boolean): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${this.baseUrl}/login`, { email, senha })
      .pipe(tap((resposta) => this.salvarToken(resposta.token, manterConectado)));
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/logout`, {}).pipe(finalize(() => this.limparSessao()));
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
