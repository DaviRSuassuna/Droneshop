import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../models/page.model';
import { StatusRef } from '../models/status.model';

export interface Avaliacao {
  id: number;
  nota: number;
  comentario: string | null;
  compraVerificada: boolean;
  status: StatusRef;
  autor: { nome: string };
  produto: { id: number; nome: string; slug: string };
  dataCadastro: string;
}

export interface AvaliacoesProduto {
  media: number | null;
  quantidade: number;
  itens: Avaliacao[];
}

export interface AvaliacaoDTO {
  nota: number;
  comentario: string;
}

/** Avaliações com compra verificada e moderação (#39). */
@Injectable({ providedIn: 'root' })
export class AvaliacaoService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  doProduto(slug: string): Observable<AvaliacoesProduto> {
    return this.http.get<AvaliacoesProduto>(
      `${this.api}/loja/produtos/${encodeURIComponent(slug)}/avaliacoes`,
    );
  }

  avaliar(slug: string, dto: AvaliacaoDTO): Observable<Avaliacao> {
    return this.http.post<Avaliacao>(
      `${this.api}/loja/produtos/${encodeURIComponent(slug)}/avaliacoes`,
      dto,
    );
  }

  listarAdmin(idStatus: number | null, page = 0, pageSize = 20): Observable<PageResponse<Avaliacao>> {
    let params = new HttpParams().set('page', page).set('pageSize', pageSize);
    if (idStatus) params = params.set('idStatus', idStatus);
    return this.http.get<PageResponse<Avaliacao>>(`${this.api}/admin/avaliacoes`, { params });
  }

  moderar(id: number, idStatus: number): Observable<void> {
    return this.http.patch<void>(`${this.api}/admin/avaliacoes/${id}`, { idStatus });
  }
}
