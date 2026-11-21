import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { FiltroDashboard, IndicadoresDashboard } from '../models/dashboard.model';

/** Indicadores do dashboard do admin (#18). */
@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/admin/dashboard`;

  /** 400 em dataFim quando o período vem invertido. */
  buscar(filtro: FiltroDashboard = {}): Observable<IndicadoresDashboard> {
    let params = new HttpParams();
    if (filtro.dataInicio) params = params.set('dataInicio', filtro.dataInicio);
    if (filtro.dataFim) params = params.set('dataFim', filtro.dataFim);
    return this.http.get<IndicadoresDashboard>(this.baseUrl, { params });
  }

  /** CSV das vendas por dia (separador ";", UTF-8 com BOM). */
  exportar(filtro: FiltroDashboard = {}): Observable<Blob> {
    let params = new HttpParams();
    if (filtro.dataInicio) params = params.set('dataInicio', filtro.dataInicio);
    if (filtro.dataFim) params = params.set('dataFim', filtro.dataFim);
    return this.http.get(`${this.baseUrl}/exportar`, { params, responseType: 'blob' });
  }
}
