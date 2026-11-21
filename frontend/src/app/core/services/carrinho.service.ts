import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Carrinho } from '../models/compra.model';

export const HEADER_CARRINHO = 'X-Carrinho-Token';
export const CHAVE_CARRINHO = 'aeria-carrinho-token';

/**
 * Carrinho anônimo por token (#24): o token fica no localStorage e vai no header
 * X-Carrinho-Token; no login o backend vincula o carrinho ao cliente (RF17).
 */
@Injectable({ providedIn: 'root' })
export class CarrinhoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/carrinho`;

  /** Quantidade de itens, para o contador da sacola no menu. */
  readonly quantidade = signal(0);

  token(): string | null {
    try {
      return localStorage.getItem(CHAVE_CARRINHO);
    } catch {
      return null;
    }
  }

  private guardar(c: Carrinho): void {
    try {
      localStorage.setItem(CHAVE_CARRINHO, c.token);
    } catch {
      // sem armazenamento: o carrinho vale só nesta aba
    }
    this.quantidade.set(c.itens.reduce((s, i) => s + i.quantidade, 0));
  }

  esquecer(): void {
    try {
      localStorage.removeItem(CHAVE_CARRINHO);
    } catch {
      // nada a fazer
    }
    this.quantidade.set(0);
  }

  private headers(): HttpHeaders {
    const t = this.token();
    return t ? new HttpHeaders({ [HEADER_CARRINHO]: t }) : new HttpHeaders();
  }

  criar(): Observable<Carrinho> {
    return this.http.post<Carrinho>(this.url, null).pipe(tap((c) => this.guardar(c)));
  }

  buscar(): Observable<Carrinho> {
    return this.http
      .get<Carrinho>(this.url, { headers: this.headers() })
      .pipe(tap((c) => this.guardar(c)));
  }

  adicionar(idProduto: number, quantidade: number): Observable<Carrinho> {
    return this.http
      .post<Carrinho>(`${this.url}/itens`, { idProduto, quantidade }, { headers: this.headers() })
      .pipe(tap((c) => this.guardar(c)));
  }

  alterar(idItem: number, quantidade: number): Observable<Carrinho> {
    return this.http
      .patch<Carrinho>(`${this.url}/itens/${idItem}`, { quantidade }, { headers: this.headers() })
      .pipe(tap((c) => this.guardar(c)));
  }

  remover(idItem: number): Observable<Carrinho> {
    return this.http
      .delete<Carrinho>(`${this.url}/itens/${idItem}`, { headers: this.headers() })
      .pipe(tap((c) => this.guardar(c)));
  }

  /** Reserva o estoque ao entrar no checkout (#23). */
  reservar(): Observable<Carrinho> {
    return this.http
      .post<Carrinho>(`${this.url}/reservar`, null, { headers: this.headers() })
      .pipe(tap((c) => this.guardar(c)));
  }

  aplicarCupom(codigo: string): Observable<Carrinho> {
    return this.http
      .post<Carrinho>(`${this.url}/cupom`, { codigo }, { headers: this.headers() })
      .pipe(tap((c) => this.guardar(c)));
  }

  removerCupom(): Observable<Carrinho> {
    return this.http
      .delete<Carrinho>(`${this.url}/cupom`, { headers: this.headers() })
      .pipe(tap((c) => this.guardar(c)));
  }

  /** Carrinho ATIVO do cliente logado (cria vazio se não houver). */
  doCliente(): Observable<Carrinho> {
    return this.http
      .get<Carrinho>(`${environment.apiUrl}/conta/carrinho`)
      .pipe(tap((c) => this.guardar(c)));
  }

  vincular(): Observable<Carrinho> {
    return this.http
      .post<Carrinho>(`${this.url}/vincular`, null, { headers: this.headers() })
      .pipe(tap((c) => this.guardar(c)));
  }
}
