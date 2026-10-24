import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { FiltroGrupo, GrupoProduto, GrupoProdutoDTO } from '../models/grupo-produto.model';
import { PageResponse } from '../models/page.model';

/** Grupos de produto (D40). O vínculo produto -> grupo é gravado pelo cadastro do produto. */
@Injectable({ providedIn: 'root' })
export class GrupoProdutoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/grupos-produto`;

  /** Uma página filtrada; nome vazio e ativo nulo são ignorados. */
  listar(filtro: FiltroGrupo = {}, page = 0, pageSize = 10): Observable<PageResponse<GrupoProduto>> {
    let params = new HttpParams().set('page', page).set('pageSize', pageSize);
    if (filtro.nome?.trim()) params = params.set('nome', filtro.nome.trim());
    if (filtro.ativo != null) params = params.set('ativo', filtro.ativo);
    return this.http.get<PageResponse<GrupoProduto>>(this.baseUrl, { params });
  }

  buscarPorId(id: number): Observable<GrupoProduto> {
    return this.http.get<GrupoProduto>(`${this.baseUrl}/${id}`);
  }

  criar(dto: GrupoProdutoDTO): Observable<GrupoProduto> {
    return this.http.post<GrupoProduto>(this.baseUrl, dto);
  }

  atualizar(id: number, dto: GrupoProdutoDTO): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${id}`, dto);
  }

  /** Grupo com produtos responde 409 (D24). */
  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
