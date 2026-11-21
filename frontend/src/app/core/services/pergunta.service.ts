import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../models/page.model';
import { StatusRef } from '../models/status.model';

/** Ids de StatusModeracao (perguntas #40 e avaliações #39). */
export const STATUS_MODERACAO = { PENDENTE: 1, APROVADA: 2, REJEITADA: 3 } as const;

export interface RespostaPergunta {
  id: number;
  texto: string;
  autor: { nome: string };
  oficial: boolean;
  dataCadastro: string;
}

export interface Pergunta {
  id: number;
  status: StatusRef;
  texto: string;
  autor: { nome: string };
  produto: { id: number; nome: string; slug: string };
  respostas: RespostaPergunta[];
  dataCadastro: string;
}

/** Perguntas e respostas sobre produtos (#40). */
@Injectable({ providedIn: 'root' })
export class PerguntaService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  aprovadas(slug: string): Observable<Pergunta[]> {
    return this.http.get<Pergunta[]>(`${this.api}/loja/produtos/${encodeURIComponent(slug)}/perguntas`);
  }

  perguntar(slug: string, texto: string): Observable<Pergunta> {
    return this.http.post<Pergunta>(`${this.api}/loja/produtos/${encodeURIComponent(slug)}/perguntas`, {
      texto,
    });
  }

  responder(id: number, texto: string): Observable<RespostaPergunta> {
    return this.http.post<RespostaPergunta>(`${this.api}/loja/perguntas/${id}/respostas`, { texto });
  }

  listarAdmin(idStatus: number | null, page = 0, pageSize = 20): Observable<PageResponse<Pergunta>> {
    let params = new HttpParams().set('page', page).set('pageSize', pageSize);
    if (idStatus) params = params.set('idStatus', idStatus);
    return this.http.get<PageResponse<Pergunta>>(`${this.api}/admin/perguntas`, { params });
  }

  moderar(id: number, idStatus: number): Observable<void> {
    return this.http.patch<void>(`${this.api}/admin/perguntas/${id}`, { idStatus });
  }

  responderOficial(id: number, texto: string): Observable<RespostaPergunta> {
    return this.http.post<RespostaPergunta>(`${this.api}/admin/perguntas/${id}/respostas`, { texto });
  }
}
