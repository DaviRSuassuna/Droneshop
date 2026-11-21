import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Devolucao, DevolucaoDTO, PagamentoDTO } from '../models/compra.model';
import { PageResponse } from '../models/page.model';
import {
  NotaFiscal,
  PagamentoPedido,
  Pedido,
  PedidoListagem,
  Rastreio,
} from '../models/pedido.model';

/** "Meus pedidos" (#29), pagamento (#27), rastreio (#31), notas (#35) e devoluções (#37) do cliente. */
@Injectable({ providedIn: 'root' })
export class ContaPedidoService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  listar(page = 0, pageSize = 10): Observable<PageResponse<PedidoListagem>> {
    const params = new HttpParams().set('page', page).set('pageSize', pageSize);
    return this.http.get<PageResponse<PedidoListagem>>(`${this.api}/conta/pedidos`, { params });
  }

  buscar(id: number): Observable<Pedido> {
    return this.http.get<Pedido>(`${this.api}/conta/pedidos/${id}`);
  }

  rastreio(id: number): Observable<Rastreio> {
    return this.http.get<Rastreio>(`${this.api}/conta/pedidos/${id}/rastreio`);
  }

  notas(id: number): Observable<NotaFiscal[]> {
    return this.http.get<NotaFiscal[]>(`${this.api}/conta/pedidos/${id}/nota-fiscal`);
  }

  /** XML ou DANFE da nota (caminho xmlUrl/danfeUrl), com o token do cliente. */
  arquivo(caminho: string): Observable<Blob> {
    return this.http.get(`${this.api}${caminho}`, { responseType: 'blob' });
  }

  pagar(id: number, dto: PagamentoDTO): Observable<PagamentoPedido> {
    return this.http.post<PagamentoPedido>(`${this.api}/pedidos/${id}/pagamentos`, dto);
  }

  devolucoes(): Observable<Devolucao[]> {
    return this.http.get<Devolucao[]>(`${this.api}/conta/devolucoes`);
  }

  solicitarDevolucao(dto: DevolucaoDTO): Observable<Devolucao> {
    return this.http.post<Devolucao>(`${this.api}/conta/devolucoes`, dto);
  }
}
