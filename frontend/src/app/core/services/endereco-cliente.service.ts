import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { EnderecoCliente, EnderecoClienteDTO } from '../models/cliente.model';

/** Endereços do cliente logado (#19). Endereço de outro cliente responde 404. */
@Injectable({ providedIn: 'root' })
export class EnderecoClienteService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/conta/enderecos`;

  listar(): Observable<EnderecoCliente[]> {
    return this.http.get<EnderecoCliente[]>(this.baseUrl);
  }

  criar(endereco: EnderecoClienteDTO): Observable<EnderecoCliente> {
    return this.http.post<EnderecoCliente>(this.baseUrl, endereco);
  }

  alterar(id: number, endereco: EnderecoClienteDTO): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${id}`, endereco);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  marcarPrincipal(id: number): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/${id}/principal`, {});
  }
}
