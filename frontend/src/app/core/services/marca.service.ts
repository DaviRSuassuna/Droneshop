import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Marca, MarcaDTO } from '../models/marca.model';

@Injectable({ providedIn: 'root' })
export class MarcaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/marcas`;

  listar(page = 0, pageSize = 500): Observable<Marca[]> {
    const params = new HttpParams().set('page', page).set('pageSize', pageSize);
    return this.http.get<Marca[]>(this.baseUrl, { params });
  }

  buscarPorId(id: number): Observable<Marca> {
    return this.http.get<Marca>(`${this.baseUrl}/${id}`);
  }

  contar(): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/count`);
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
