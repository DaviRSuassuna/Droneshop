import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ItemListaDesejos } from '../models/compra.model';

/** Lista de desejos do cliente logado (#22). */
@Injectable({ providedIn: 'root' })
export class ListaDesejosService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/conta/desejos`;

  listar(): Observable<ItemListaDesejos[]> {
    return this.http.get<ItemListaDesejos[]>(this.url);
  }

  adicionar(idProduto: number): Observable<void> {
    return this.http.post<void>(this.url, { idProduto });
  }

  remover(idProduto: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${idProduto}`);
  }
}
