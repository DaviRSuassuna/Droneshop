import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../models/page.model';
import {
  FiltroProduto,
  Produto,
  ProdutoLoteRequest,
  ProdutoLoteResponse,
} from '../models/produto.model';

/** Listagem, consulta e exclusão de todos os tipos. Cadastro e alteração ficam em DroneService, AcessorioService e KitService (D27). */
@Injectable({ providedIn: 'root' })
export class ProdutoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/produtos`;

  /** Uma página já filtrada no servidor, com o total em totalItems. */
  listar(filtro: FiltroProduto, page = 0, pageSize = 10): Observable<PageResponse<Produto>> {
    const params = this.paramsDoFiltro(filtro).set('page', page).set('pageSize', pageSize);
    return this.http.get<PageResponse<Produto>>(this.baseUrl, { params });
  }

  alterarEmLote(dto: ProdutoLoteRequest): Observable<ProdutoLoteResponse> {
    return this.http.patch<ProdutoLoteResponse>(this.baseUrl + '/lote', dto);
  }

  exportar(filtro: FiltroProduto = {}): Observable<Blob> {
    return this.http.get(this.baseUrl + '/exportar', {
      params: this.paramsDoFiltro(filtro),
      responseType: 'blob',
    });
  }

  buscarPorId(id: number): Observable<Produto> {
    return this.http.get<Produto>(`${this.baseUrl}/${id}`);
  }

  buscarPorSlug(slug: string): Observable<Produto> {
    return this.http.get<Produto>(`${this.baseUrl}/slug/${encodeURIComponent(slug)}`);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  private paramsDoFiltro(filtro: FiltroProduto): HttpParams {
    let params = new HttpParams();
    if (filtro.idTipo) params = params.set('idTipo', filtro.idTipo);
    if (filtro.busca?.trim()) params = params.set('busca', filtro.busca.trim());
    if (filtro.idMarca) params = params.set('idMarca', filtro.idMarca);
    if (filtro.idCategoria) params = params.set('idCategoria', filtro.idCategoria);
    if (filtro.idSubcategoria) params = params.set('idSubcategoria', filtro.idSubcategoria);
    if (filtro.idStatus) params = params.set('idStatus', filtro.idStatus);
    if (filtro.idGrupo) params = params.set('idGrupo', filtro.idGrupo);
    if (filtro.kit === false) params = params.set('kit', false);
    if (filtro.estoque) params = params.set('estoque', filtro.estoque);
    return params;
  }
}
