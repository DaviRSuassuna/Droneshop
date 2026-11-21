import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../models/page.model';
import { TermoBuscado } from '../models/termo-buscado.model';

export interface FiltroTermos {
  semResultado?: boolean;
  dataInicio?: string;
  dataFim?: string;
}

@Injectable({ providedIn: 'root' })
export class TermoBuscadoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/admin/termos-buscados`;

  listar(filtro: FiltroTermos, page = 0, pageSize = 20): Observable<PageResponse<TermoBuscado>> {
    let params = new HttpParams().set('page', page).set('pageSize', pageSize);
    if (filtro.semResultado) params = params.set('semResultado', true);
    if (filtro.dataInicio) params = params.set('dataInicio', filtro.dataInicio);
    if (filtro.dataFim) params = params.set('dataFim', filtro.dataFim);
    return this.http.get<PageResponse<TermoBuscado>>(this.url, { params });
  }
}
