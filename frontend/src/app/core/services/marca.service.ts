import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../models/page.model';
import { Marca, MarcaDTO } from '../models/marca.model';

@Injectable({ providedIn: 'root' })
export class MarcaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/marcas`;

  listar(page = 0, pageSize = 10): Observable<PageResponse<Marca>> {
    const params = new HttpParams().set('page', page).set('pageSize', pageSize);
    return this.http.get<PageResponse<Marca>>(this.baseUrl, { params });
  }

  buscarPorNome(nome: string, page = 0, pageSize = 10): Observable<PageResponse<Marca>> {
    const params = new HttpParams().set('page', page).set('pageSize', pageSize);
    return this.http.get<PageResponse<Marca>>(`${this.baseUrl}/nome/${encodeURIComponent(nome)}`, { params });
  }

  buscarPorId(id: number): Observable<Marca> {
    return this.http.get<Marca>(`${this.baseUrl}/${id}`);
  }

  criar(dto: MarcaDTO): Observable<Marca> {
    return this.http.post<Marca>(this.baseUrl, dto);
  }

  atualizar(id: number, dto: MarcaDTO): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${id}`, dto);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
