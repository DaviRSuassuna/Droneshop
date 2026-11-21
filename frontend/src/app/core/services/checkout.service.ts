import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CheckoutDTO, CotacaoFrete } from '../models/compra.model';
import { Pedido } from '../models/pedido.model';
import { CarrinhoService, HEADER_CARRINHO } from './carrinho.service';

/** Cotação de frete do carrinho (#30) e confirmação do checkout (#26). */
@Injectable({ providedIn: 'root' })
export class CheckoutService {
  private readonly http = inject(HttpClient);
  private readonly carrinho = inject(CarrinhoService);
  private readonly api = environment.apiUrl;

  private headers(): HttpHeaders {
    const t = this.carrinho.token();
    return t ? new HttpHeaders({ [HEADER_CARRINHO]: t }) : new HttpHeaders();
  }

  /** Cota o carrinho do header para o CEP (8 dígitos). */
  cotar(cep: string): Observable<CotacaoFrete[]> {
    return this.http.post<CotacaoFrete[]>(
      `${this.api}/frete/cotacao`,
      { cep },
      { headers: this.headers() },
    );
  }

  confirmar(dto: CheckoutDTO): Observable<Pedido> {
    return this.http.post<Pedido>(`${this.api}/checkout`, dto, { headers: this.headers() });
  }
}
