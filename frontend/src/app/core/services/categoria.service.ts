import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Categoria, CategoriaDTO } from '../models/categoria.model';

@Injectable({ providedIn: 'root' })
export class CategoriaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/categorias`;

  listar(page = 0, pageSize = 500): Observable<Categoria[]> {
    const params = new HttpParams().set('page', page).set('pageSize', pageSize);
    return this.http.get<Categoria[]>(this.baseUrl, { params });
  }
  buscarPorId(id: number): Observable<Categoria> { return this.http.get<Categoria>(`${this.baseUrl}/${id}`); }
  criar(dto: CategoriaDTO): Observable<Categoria> { return this.http.post<Categoria>(this.baseUrl, dto); }
  atualizar(id: number, dto: CategoriaDTO): Observable<void> { return this.http.put<void>(`${this.baseUrl}/${id}`, dto); }
  excluir(id: number): Observable<void> { return this.http.delete<void>(`${this.baseUrl}/${id}`); }
}
