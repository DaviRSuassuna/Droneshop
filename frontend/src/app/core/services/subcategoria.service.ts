import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Subcategoria, SubcategoriaDTO } from '../models/subcategoria.model';

@Injectable({ providedIn: 'root' })
export class SubcategoriaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/subcategorias`;

  /** Sem categoriaId devolve todas; com ele, só as subcategorias daquela categoria. */
  listar(categoriaId?: number | null, page = 0, pageSize = 500): Observable<Subcategoria[]> {
    let params = new HttpParams().set('page', page).set('pageSize', pageSize);
    if (categoriaId) params = params.set('categoriaId', categoriaId);
    return this.http.get<Subcategoria[]>(this.baseUrl, { params });
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
