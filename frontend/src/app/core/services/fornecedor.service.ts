import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Fornecedor, FornecedorDTO } from '../models/fornecedor.model';

@Injectable({ providedIn: 'root' })
export class FornecedorService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/fornecedores`;

  listar(page = 0, pageSize = 500): Observable<Fornecedor[]> {
    const params = new HttpParams().set('page', page).set('pageSize', pageSize);
    return this.http.get<Fornecedor[]>(this.baseUrl, { params });
  }

  buscarPorId(id: number): Observable<Fornecedor> {
    return this.http.get<Fornecedor>(`${this.baseUrl}/${id}`);
  }

  contar(): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/count`);
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
