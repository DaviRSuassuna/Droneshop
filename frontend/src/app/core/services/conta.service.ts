import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CartaoSalvo,
  DroneDaConta,
  ListaCompartilhada,
  MoverParaCarrinho,
} from '../models/compra.model';

export interface Termo {
  tipo: string;
  versao: string;
  obrigatorio: boolean;
}

export interface Consentimento {
  tipo: string;
  versao: string;
  dataConsentimento: string;
}

/**
 * Áreas da conta além do perfil: cartões (#28), drones (#88), LGPD (#46), 2FA (#45)
 * e as ações de favoritos (#86).
 */
@Injectable({ providedIn: 'root' })
export class ContaService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  cartoes(): Observable<CartaoSalvo[]> {
    return this.http.get<CartaoSalvo[]>(`${this.api}/conta/cartoes`);
  }

  salvarCartao(c: {
    tokenGateway: string;
    ultimosDigitos: string;
    bandeira: string;
    validade: string;
  }): Observable<CartaoSalvo> {
    return this.http.post<CartaoSalvo>(`${this.api}/conta/cartoes`, c);
  }

  excluirCartao(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/conta/cartoes/${id}`);
  }

  tornarPadrao(id: number): Observable<void> {
    return this.http.patch<void>(`${this.api}/conta/cartoes/${id}/padrao`, null);
  }

  drones(): Observable<DroneDaConta[]> {
    return this.http.get<DroneDaConta[]>(`${this.api}/conta/drones`);
  }

  registrarAnac(idUnidade: number, registroAnac: string): Observable<void> {
    return this.http.patch<void>(`${this.api}/conta/drones/${idUnidade}`, { registroAnac });
  }

  termos(): Observable<Termo[]> {
    return this.http.get<Termo[]>(`${this.api}/loja/termos`);
  }

  consentimentos(): Observable<Consentimento[]> {
    return this.http.get<Consentimento[]>(`${this.api}/conta/consentimentos`);
  }

  consentir(tipo: string, versao: string): Observable<void> {
    return this.http.post<void>(`${this.api}/conta/consentimentos`, { tipo, versao });
  }

  excluirConta(senha: string): Observable<void> {
    return this.http.post<void>(`${this.api}/conta/exclusao`, { senha });
  }

  moverFavoritosParaCarrinho(): Observable<MoverParaCarrinho> {
    return this.http.post<MoverParaCarrinho>(`${this.api}/conta/desejos/mover-para-carrinho`, null);
  }

  compartilharFavoritos(): Observable<{ token: string; url: string }> {
    return this.http.post<{ token: string; url: string }>(
      `${this.api}/conta/desejos/compartilhar`,
      null,
    );
  }

  desligarCompartilhamento(): Observable<void> {
    return this.http.delete<void>(`${this.api}/conta/desejos/compartilhar`);
  }

  listaCompartilhada(token: string): Observable<ListaCompartilhada> {
    return this.http.get<ListaCompartilhada>(`${this.api}/loja/listas/${token}`);
  }
}
