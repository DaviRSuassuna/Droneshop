import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { KitDTO, Produto } from '../models/produto.model';

/** Cadastro e alteração em /kits; consulta e exclusão ficam no ProdutoService. */
@Injectable({ providedIn: 'root' })
export class KitService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/kits`;

  criar(dto: KitDTO): Observable<Produto> {
    return this.http.post<Produto>(this.baseUrl, dto);
  }

  atualizar(id: number, dto: KitDTO): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${id}`, dto);
  }
}
