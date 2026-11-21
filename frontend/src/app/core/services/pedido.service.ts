import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../models/page.model';
import {
  FiltroPedidos,
  HistoricoStatusPedido,
  IndicadoresPedidos,
  Pedido,
  PedidoListagem,
  ResumoStatusPedido,
} from '../models/pedido.model';

/** Pedidos no admin: consulta e mudança de status (o pedido nasce no checkout). */
@Injectable({ providedIn: 'root' })
export class PedidoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/pedidos`;

  etiqueta(id: number): Observable<Blob> {
    return this.http.get(this.baseUrl + '/' + id + '/etiqueta', { responseType: 'blob' });
  }

  /** Um PDF com uma etiqueta por pedido, na ordem dos ids (#89); 409 se algum não tem endereço. */
  etiquetas(ids: number[]): Observable<Blob> {
    return this.http.post(this.baseUrl + '/etiquetas', { ids }, { responseType: 'blob' });
  }

  indicadores(): Observable<IndicadoresPedidos> {
    return this.http.get<IndicadoresPedidos>(`${this.baseUrl}/indicadores`);
  }

  exportar(filtro: FiltroPedidos = {}): Observable<Blob> {
    const params = this.filtros(new HttpParams(), filtro);
    return this.http.get(this.baseUrl + '/exportar', { params, responseType: 'blob' });
  }

  /** Filtros vazios não vão para a API. */
  private filtros(params: HttpParams, filtro: FiltroPedidos): HttpParams {
    if (filtro.idStatus) params = params.set('idStatus', filtro.idStatus);
    if (filtro.dataInicio) params = params.set('dataInicio', filtro.dataInicio);
    if (filtro.dataFim) params = params.set('dataFim', filtro.dataFim);
    const busca = filtro.busca?.trim();
    if (busca) params = params.set('busca', busca);
    if (filtro.idMetodoPagamento) params = params.set('idMetodoPagamento', filtro.idMetodoPagamento);
    return params;
  }

  listar(
    filtro: FiltroPedidos = {},
    page = 0,
    pageSize = 10,
  ): Observable<PageResponse<PedidoListagem>> {
    const params = this.filtros(
      new HttpParams().set('page', page).set('pageSize', pageSize),
      filtro,
    );
    return this.http.get<PageResponse<PedidoListagem>>(this.baseUrl, { params });
  }

  resumoStatus(): Observable<ResumoStatusPedido[]> {
    return this.http.get<ResumoStatusPedido[]>(`${this.baseUrl}/resumo-status`);
  }

  buscarPorId(id: number): Observable<Pedido> {
    return this.http.get<Pedido>(`${this.baseUrl}/${id}`);
  }

  /** 204; 409 quando a transição não é permitida a partir do status atual (D30). */
  alterarStatus(id: number, idStatus: number, observacao?: string | null): Observable<void> {
    const texto = observacao?.trim();
    return this.http.patch<void>(`${this.baseUrl}/${id}/status`, {
      idStatus,
      observacao: texto ? texto : null,
    });
  }

  historico(id: number): Observable<HistoricoStatusPedido[]> {
    return this.http.get<HistoricoStatusPedido[]>(`${this.baseUrl}/${id}/historico`);
  }
}
