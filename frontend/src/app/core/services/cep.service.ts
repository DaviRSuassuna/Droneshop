import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CepResultado } from '../models/endereco.model';
import { somenteDigitos } from '../utils/documentos';

/**
 * Consulta de CEP. O front fala só com o backend; quem chama o ViaCEP é a API.
 * 200 = encontrado | 400 = formato inválido | 404 = não existe | 503 = ViaCEP indisponível
 */
@Injectable({ providedIn: 'root' })
export class CepService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/ceps`;

  buscar(cep: string): Observable<CepResultado> {
    return this.http.get<CepResultado>(`${this.baseUrl}/${somenteDigitos(cep)}`);
  }
}
