import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { FiltroProduto, Produto, ProdutoDTO } from '../models/produto.model';

@Injectable({ providedIn: 'root' })
export class ProdutoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/produtos`;

  /** Uma página já filtrada no servidor. */
  listar(filtro: FiltroProduto, page = 0, pageSize = 10): Observable<Produto[]> {
    const params = this.paramsDoFiltro(filtro).set('page', page).set('pageSize', pageSize);
    return this.http.get<Produto[]>(this.baseUrl, { params });
  }

  /** Total de registros para o mesmo filtro (usado na paginação). */
  contar(filtro: FiltroProduto): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/count`, { params: this.paramsDoFiltro(filtro) });
  }

  buscarPorId(id: number): Observable<Produto> {
    return this.http.get<Produto>(`${this.baseUrl}/${id}`);
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
    if (filtro.marcaId) params = params.set('marcaId', filtro.marcaId);
    if (filtro.categoriaId) params = params.set('categoriaId', filtro.categoriaId);
    if (filtro.subcategoriaId) params = params.set('subcategoriaId', filtro.subcategoriaId);
    if (filtro.status) params = params.set('status', filtro.status);
    return params;
  }
}
