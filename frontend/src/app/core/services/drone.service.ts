import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DroneDTO, Produto } from '../models/produto.model';

/** Cadastro e alteração em /drones; consulta e exclusão ficam no ProdutoService. */
@Injectable({ providedIn: 'root' })
export class DroneService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/drones`;

  criar(dto: DroneDTO): Observable<Produto> {
    return this.http.post<Produto>(this.baseUrl, dto);
  }

  atualizar(id: number, dto: DroneDTO): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${id}`, dto);
  }
}
