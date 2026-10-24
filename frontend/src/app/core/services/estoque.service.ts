import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../models/page.model';
import {
  FiltroEstoque,
  FiltroMovimentos,
  ItemEstoque,
  MovimentoEstoque,
  MovimentoEstoqueRequest,
} from '../models/estoque.model';

/** Saldo por produto e movimentos de estoque (#13). Movimento não se edita nem se exclui. */
@Injectable({ providedIn: 'root' })
export class EstoqueService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/estoque`;

  exportar(filtro: FiltroEstoque = {}): Observable<Blob> {
    let params = new HttpParams();
    if (filtro.situacao) params = params.set('situacao', filtro.situacao);
    if (filtro.busca?.trim()) params = params.set('busca', filtro.busca.trim());
    if (filtro.idMarca) params = params.set('idMarca', filtro.idMarca);
    return this.http.get(this.baseUrl + '/exportar', { params, responseType: 'blob' });
  }

  listar(
    filtro: FiltroEstoque = {},
    page = 0,
    pageSize = 10,
  ): Observable<PageResponse<ItemEstoque>> {
    let params = new HttpParams().set('page', page).set('pageSize', pageSize);
    if (filtro.situacao && filtro.situacao !== 'TODOS')
      params = params.set('situacao', filtro.situacao);
    const busca = filtro.busca?.trim();
    if (busca) params = params.set('busca', busca);
    if (filtro.idMarca) params = params.set('idMarca', filtro.idMarca);
    return this.http.get<PageResponse<ItemEstoque>>(this.baseUrl, { params });
  }

  listarMovimentos(
    filtro: FiltroMovimentos = {},
    page = 0,
    pageSize = 10,
  ): Observable<PageResponse<MovimentoEstoque>> {
    let params = new HttpParams().set('page', page).set('pageSize', pageSize);
    if (filtro.idProduto) params = params.set('idProduto', filtro.idProduto);
    if (filtro.idTipo) params = params.set('idTipo', filtro.idTipo);
    return this.http.get<PageResponse<MovimentoEstoque>>(`${this.baseUrl}/movimentos`, { params });
  }

  registrarMovimento(dto: MovimentoEstoqueRequest): Observable<MovimentoEstoque> {
    return this.http.post<MovimentoEstoque>(`${this.baseUrl}/movimentos`, dto);
  }
}
