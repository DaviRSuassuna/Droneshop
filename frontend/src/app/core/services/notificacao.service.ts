import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Notificacao, TemplateEmail, TemplateEmailDTO } from '../models/notificacao.model';
import { PageResponse } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class NotificacaoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/admin`;

  templates(): Observable<TemplateEmail[]> {
    return this.http.get<TemplateEmail[]>(`${this.url}/templates-email`);
  }

  template(codigo: string): Observable<TemplateEmail> {
    return this.http.get<TemplateEmail>(`${this.url}/templates-email/${codigo}`);
  }

  editar(codigo: string, dto: TemplateEmailDTO): Observable<TemplateEmail> {
    return this.http.put<TemplateEmail>(`${this.url}/templates-email/${codigo}`, dto);
  }

  notificacoes(filtro: {
    destinatario?: string;
    idStatusEnvio?: number | null;
    page?: number;
    pageSize?: number;
  }): Observable<PageResponse<Notificacao>> {
    let params = new HttpParams()
      .set('page', filtro.page ?? 0)
      .set('pageSize', filtro.pageSize ?? 20);
    if (filtro.destinatario?.trim()) params = params.set('destinatario', filtro.destinatario.trim());
    if (filtro.idStatusEnvio) params = params.set('idStatusEnvio', filtro.idStatusEnvio);
    return this.http.get<PageResponse<Notificacao>>(`${this.url}/notificacoes`, { params });
  }
}
