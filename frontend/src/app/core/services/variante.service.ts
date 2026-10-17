import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../models/page.model';
import { FiltroVariante, Variante, VarianteListagem } from '../models/produto.model';

/** Consulta de variantes. Criar, alterar e excluir é pelo corpo do drone/acessório (D13). */
@Injectable({ providedIn: 'root' })
export class VarianteService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/variantes`;

  /** Uma página já filtrada no servidor (busca no SKU, no nome da variante e no do produto). */
  listar(
    filtro: FiltroVariante,
    page = 0,
    pageSize = 10,
  ): Observable<PageResponse<VarianteListagem>> {
    let params = new HttpParams().set('page', page).set('pageSize', pageSize);
    if (filtro.busca?.trim()) params = params.set('busca', filtro.busca.trim());
    if (filtro.ativa != null) params = params.set('ativa', filtro.ativa);
    return this.http.get<PageResponse<VarianteListagem>>(this.baseUrl, { params });
  }

  buscarPorId(id: number): Observable<Variante> {
    return this.http.get<Variante>(`${this.baseUrl}/${id}`);
  }
}
