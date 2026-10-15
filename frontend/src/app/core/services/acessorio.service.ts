import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AcessorioDTO, Produto } from '../models/produto.model';

/** Cadastro e alteração em /acessorios; consulta e exclusão ficam no ProdutoService. */
@Injectable({ providedIn: 'root' })
export class AcessorioService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/acessorios`;

  criar(dto: AcessorioDTO): Observable<Produto> {
    return this.http.post<Produto>(this.baseUrl, dto);
  }

  atualizar(id: number, dto: AcessorioDTO): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${id}`, dto);
  }
}
