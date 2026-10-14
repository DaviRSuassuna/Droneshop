import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../models/page.model';
import { Subcategoria, SubcategoriaDTO } from '../models/subcategoria.model';

@Injectable({ providedIn: 'root' })
export class SubcategoriaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/subcategorias`;

  /** Sem idCategoria devolve todas; com ele, só as subcategorias daquela categoria. */
  listar(idCategoria?: number | null, page = 0, pageSize = 10): Observable<PageResponse<Subcategoria>> {
    let params = new HttpParams().set('page', page).set('pageSize', pageSize);
    if (idCategoria) params = params.set('idCategoria', idCategoria);
    return this.http.get<PageResponse<Subcategoria>>(this.baseUrl, { params });
  }

  buscarPorId(id: number): Observable<Subcategoria> {
    return this.http.get<Subcategoria>(`${this.baseUrl}/${id}`);
  }

  criar(dto: SubcategoriaDTO): Observable<Subcategoria> {
    return this.http.post<Subcategoria>(this.baseUrl, dto);
  }

  atualizar(id: number, dto: SubcategoriaDTO): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${id}`, dto);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
