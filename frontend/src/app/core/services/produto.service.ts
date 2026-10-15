import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../models/page.model';
import { FiltroProduto, Produto, ProdutoDTO } from '../models/produto.model';

@Injectable({ providedIn: 'root' })
export class ProdutoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/produtos`;

  /** Uma página já filtrada no servidor, com o total em totalItems. */
  listar(filtro: FiltroProduto, page = 0, pageSize = 10): Observable<PageResponse<Produto>> {
    const params = this.paramsDoFiltro(filtro).set('page', page).set('pageSize', pageSize);
    return this.http.get<PageResponse<Produto>>(this.baseUrl, { params });
  }

  buscarPorId(id: number): Observable<Produto> {
    return this.http.get<Produto>(`${this.baseUrl}/${id}`);
  }

  buscarPorSlug(slug: string): Observable<Produto> {
    return this.http.get<Produto>(`${this.baseUrl}/slug/${encodeURIComponent(slug)}`);
  }

  criar(dto: ProdutoDTO): Observable<Produto> {
    return this.http.post<Produto>(this.baseUrl, dto);
  }

  atualizar(id: number, dto: ProdutoDTO): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${id}`, dto);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  private paramsDoFiltro(filtro: FiltroProduto): HttpParams {
    let params = new HttpParams();
    if (filtro.nome?.trim()) params = params.set('nome', filtro.nome.trim());
    if (filtro.idMarca) params = params.set('idMarca', filtro.idMarca);
    if (filtro.idCategoria) params = params.set('idCategoria', filtro.idCategoria);
    if (filtro.idSubcategoria) params = params.set('idSubcategoria', filtro.idSubcategoria);
    if (filtro.idStatus) params = params.set('idStatus', filtro.idStatus);
    return params;
  }
}
