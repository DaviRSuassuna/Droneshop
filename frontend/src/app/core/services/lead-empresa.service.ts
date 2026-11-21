import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../models/page.model';
import { StatusRef } from '../models/status.model';

/** Ids de StatusLead. */
export const STATUS_LEAD = { NOVO: 1, EM_CONTATO: 2, FECHADO: 3, DESCARTADO: 4 } as const;

export interface LeadEmpresaDTO {
  razaoSocial: string;
  cnpj: string;
  nomeContato: string;
  emailContato: string;
  telefoneContato: string | null;
  segmento: string;
  quantidadeEstimada: number | null;
  mensagem: string | null;
}

export interface LeadEmpresa extends LeadEmpresaDTO {
  id: number;
  status: StatusRef;
  dataCadastro: string;
}

/** Proposta comercial para empresas (D36, issue nova-empresas). */
@Injectable({ providedIn: 'root' })
export class LeadEmpresaService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  enviar(dto: LeadEmpresaDTO): Observable<void> {
    return this.http.post<void>(`${this.api}/leads-empresas`, dto);
  }

  listar(idStatus: number | null, page = 0, pageSize = 20): Observable<PageResponse<LeadEmpresa>> {
    let params = new HttpParams().set('page', page).set('pageSize', pageSize);
    if (idStatus) params = params.set('idStatus', idStatus);
    return this.http.get<PageResponse<LeadEmpresa>>(`${this.api}/admin/leads-empresas`, { params });
  }

  alterarStatus(id: number, idStatus: number): Observable<void> {
    return this.http.patch<void>(`${this.api}/admin/leads-empresas/${id}/status`, { idStatus });
  }
}
