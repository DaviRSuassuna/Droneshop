import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Estado } from '../models/endereco.model';
import { PageResponse } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class EstadoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/estados`;

  /** São 27 UFs: a página padrão do select já traz todas. */
  listar(page = 0, pageSize = 27): Observable<PageResponse<Estado>> {
    const params = new HttpParams().set('page', page).set('pageSize', pageSize);
    return this.http.get<PageResponse<Estado>>(this.baseUrl, { params });
  }
}
