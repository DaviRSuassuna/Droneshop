import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Chamado, ChamadoDTO, MensagemChamado } from '../models/chamado.model';
import { PageResponse } from '../models/page.model';

/** Chamados de suporte (#38): rotas do cliente e do atendente. */
@Injectable({ providedIn: 'root' })
export class ChamadoService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  abrir(dto: ChamadoDTO): Observable<Chamado> {
    return this.http.post<Chamado>(`${this.api}/chamados`, dto);
  }

  meus(): Observable<Chamado[]> {
    return this.http.get<Chamado[]>(`${this.api}/conta/chamados`);
  }

  mensagens(id: number): Observable<MensagemChamado[]> {
    return this.http.get<MensagemChamado[]>(`${this.api}/chamados/${id}/mensagens`);
  }

  responder(id: number, texto: string): Observable<MensagemChamado> {
    return this.http.post<MensagemChamado>(`${this.api}/chamados/${id}/mensagens`, { texto });
  }

  listarAdmin(idStatus: number | null, page = 0, pageSize = 20): Observable<PageResponse<Chamado>> {
    let params = new HttpParams().set('page', page).set('pageSize', pageSize);
    if (idStatus) params = params.set('idStatus', idStatus);
    return this.http.get<PageResponse<Chamado>>(`${this.api}/admin/chamados`, { params });
  }

  buscarAdmin(id: number): Observable<Chamado> {
    return this.http.get<Chamado>(`${this.api}/admin/chamados/${id}`);
  }

  atender(id: number, texto: string): Observable<MensagemChamado> {
    return this.http.post<MensagemChamado>(`${this.api}/admin/chamados/${id}/mensagens`, { texto });
  }

  alterarStatus(id: number, idStatus: number): Observable<void> {
    return this.http.patch<void>(`${this.api}/admin/chamados/${id}/status`, { idStatus });
  }
}
