import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../models/page.model';
import { Promocao, PromocaoDTO } from '../models/promocao.model';

@Injectable({ providedIn: 'root' })
export class PromocaoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/promocoes`;

  listar(situacao = '', page = 0, pageSize = 10): Observable<PageResponse<Promocao>> {
    let params = new HttpParams().set('page', page).set('pageSize', pageSize);
    if (situacao.trim()) params = params.set('situacao', situacao.trim());
    return this.http.get<PageResponse<Promocao>>(this.baseUrl, { params });
  }

  buscarPorId(id: number): Observable<Promocao> {
    return this.http.get<Promocao>(`${this.baseUrl}/${id}`);
  }

  criar(dto: PromocaoDTO): Observable<Promocao> {
    return this.http.post<Promocao>(this.baseUrl, dto);
  }

  atualizar(id: number, dto: PromocaoDTO): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${id}`, dto);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
