import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../models/page.model';
import { StatusRef } from '../models/status.model';

/** Linha do log de auditoria (#41): uma por campo alterado. */
export interface LogAuditoria {
  acao: StatusRef;
  entidade: string;
  entidadeId: string | null;
  campo: string | null;
  valorAnterior: string | null;
  valorNovo: string | null;
  autor: { id: number; nome: string } | null;
  ip: string | null;
  dataHora: string;
}

export interface FiltroAuditoria {
  entidade?: string;
  entidadeId?: string;
  idAcao?: number | null;
  idUsuario?: number | null;
}

@Injectable({ providedIn: 'root' })
export class AuditoriaService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  acoes(): Observable<StatusRef[]> {
    return this.http.get<StatusRef[]>(`${this.api}/acoes-auditoria`);
  }

  listar(filtro: FiltroAuditoria, page = 0, pageSize = 20): Observable<PageResponse<LogAuditoria>> {
    let params = new HttpParams().set('page', page).set('pageSize', pageSize);
    for (const [chave, valor] of Object.entries(filtro)) {
      if (valor === null || valor === undefined || `${valor}`.trim() === '') continue;
      params = params.set(chave, `${valor}`.trim());
    }
    return this.http.get<PageResponse<LogAuditoria>>(`${this.api}/admin/auditoria`, { params });
  }
}
