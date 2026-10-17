import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../models/page.model';
import { Cupom, CupomDTO, UsoCupom } from '../models/cupom.model';

@Injectable({ providedIn: 'root' })
export class CupomService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/cupons`;

  listar(situacao = '', codigo = '', page = 0, pageSize = 10): Observable<PageResponse<Cupom>> {
    let params = new HttpParams().set('page', page).set('pageSize', pageSize);
    if (situacao.trim()) params = params.set('situacao', situacao.trim());
    if (codigo.trim()) params = params.set('codigo', codigo.trim());
    return this.http.get<PageResponse<Cupom>>(this.baseUrl, { params });
  }

  buscarPorId(id: number): Observable<Cupom> {
    return this.http.get<Cupom>(`${this.baseUrl}/${id}`);
  }

  criar(dto: CupomDTO): Observable<Cupom> {
    return this.http.post<Cupom>(this.baseUrl, dto);
  }

  atualizar(id: number, dto: CupomDTO): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${id}`, dto);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  usos(id: number, page = 0, pageSize = 1000): Observable<PageResponse<UsoCupom>> {
    const params = new HttpParams().set('page', page).set('pageSize', pageSize);
    return this.http.get<PageResponse<UsoCupom>>(`${this.baseUrl}/${id}/usos`, { params });
  }
}
