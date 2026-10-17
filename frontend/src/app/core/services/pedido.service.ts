import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../models/page.model';
import { FiltroPedidos, Pedido, PedidoListagem, ResumoStatusPedido } from '../models/pedido.model';

/** Pedidos no admin: só leitura nesta fase (D19). */
@Injectable({ providedIn: 'root' })
export class PedidoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/pedidos`;

  listar(
    filtro: FiltroPedidos = {},
    page = 0,
    pageSize = 10,
  ): Observable<PageResponse<PedidoListagem>> {
    let params = new HttpParams().set('page', page).set('pageSize', pageSize);
    if (filtro.idStatus) params = params.set('idStatus', filtro.idStatus);
    if (filtro.dataInicio) params = params.set('dataInicio', filtro.dataInicio);
    if (filtro.dataFim) params = params.set('dataFim', filtro.dataFim);
    const busca = filtro.busca?.trim();
    if (busca) params = params.set('busca', busca);
    return this.http.get<PageResponse<PedidoListagem>>(this.baseUrl, { params });
  }

  resumoStatus(): Observable<ResumoStatusPedido[]> {
    return this.http.get<ResumoStatusPedido[]>(`${this.baseUrl}/resumo-status`);
  }

  buscarPorId(id: number): Observable<Pedido> {
    return this.http.get<Pedido>(`${this.baseUrl}/${id}`);
  }
}
