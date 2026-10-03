import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Municipio } from '../models/endereco.model';

@Injectable({ providedIn: 'root' })
export class MunicipioService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/municipios`;

  /** Municípios de um estado, em ordem alfabética (a API exige o estado). */
  listarPorEstado(idEstado: number): Observable<Municipio[]> {
    const params = new HttpParams().set('idEstado', idEstado);
    return this.http.get<Municipio[]>(this.baseUrl, { params });
  }
}
