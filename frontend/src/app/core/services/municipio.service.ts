import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Municipio } from '../models/endereco.model';
import { PageResponse } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class MunicipioService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/municipios`;

  /** Municípios de um estado, em ordem alfabética (a API exige o estado). O maior estado tem 853. */
  listarPorEstado(idEstado: number, page = 0, pageSize = 1000): Observable<PageResponse<Municipio>> {
    const params = new HttpParams().set('idEstado', idEstado).set('page', page).set('pageSize', pageSize);
    return this.http.get<PageResponse<Municipio>>(this.baseUrl, { params });
  }
}
