import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../models/page.model';
import { Fornecedor, FornecedorDTO } from '../models/fornecedor.model';

@Injectable({ providedIn: 'root' })
export class FornecedorService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/fornecedores`;

  exportar(
    filtro: { busca?: string; idStatus?: number | null; uf?: string } = {},
  ): Observable<Blob> {
    let params = new HttpParams();
    if (filtro.busca?.trim()) params = params.set('busca', filtro.busca.trim());
    if (filtro.idStatus) params = params.set('idStatus', filtro.idStatus);
    if (filtro.uf) params = params.set('uf', filtro.uf);
    return this.http.get(this.baseUrl + '/exportar', { params, responseType: 'blob' });
  }

  listar(page = 0, pageSize = 10): Observable<PageResponse<Fornecedor>> {
    const params = new HttpParams().set('page', page).set('pageSize', pageSize);
    return this.http.get<PageResponse<Fornecedor>>(this.baseUrl, { params });
  }

  buscarPorNome(nome: string, page = 0, pageSize = 10): Observable<PageResponse<Fornecedor>> {
    const params = new HttpParams().set('page', page).set('pageSize', pageSize);
    return this.http.get<PageResponse<Fornecedor>>(
      `${this.baseUrl}/nome/${encodeURIComponent(nome)}`,
      { params },
    );
  }

  buscarPorId(id: number): Observable<Fornecedor> {
    return this.http.get<Fornecedor>(`${this.baseUrl}/${id}`);
  }

  criar(dto: FornecedorDTO): Observable<Fornecedor> {
    return this.http.post<Fornecedor>(this.baseUrl, dto);
  }

  atualizar(id: number, dto: FornecedorDTO): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${id}`, dto);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
