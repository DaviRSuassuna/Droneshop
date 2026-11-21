import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../models/page.model';
import { Marca, MarcaDTO, MarcaLogo } from '../models/marca.model';

@Injectable({ providedIn: 'root' })
export class MarcaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/marcas`;

  exportar(
    filtro: { busca?: string; idStatus?: number | null; paisOrigem?: string } = {},
  ): Observable<Blob> {
    let params = new HttpParams();
    if (filtro.busca?.trim()) params = params.set('busca', filtro.busca.trim());
    if (filtro.idStatus) params = params.set('idStatus', filtro.idStatus);
    if (filtro.paisOrigem) params = params.set('paisOrigem', filtro.paisOrigem);
    return this.http.get(this.baseUrl + '/exportar', { params, responseType: 'blob' });
  }

  listar(page = 0, pageSize = 10): Observable<PageResponse<Marca>> {
    const params = new HttpParams().set('page', page).set('pageSize', pageSize);
    return this.http.get<PageResponse<Marca>>(this.baseUrl, { params });
  }

  buscarPorNome(nome: string, page = 0, pageSize = 10): Observable<PageResponse<Marca>> {
    const params = new HttpParams().set('page', page).set('pageSize', pageSize);
    return this.http.get<PageResponse<Marca>>(`${this.baseUrl}/nome/${encodeURIComponent(nome)}`, {
      params,
    });
  }

  buscarPorId(id: number): Observable<Marca> {
    return this.http.get<Marca>(`${this.baseUrl}/${id}`);
  }

  criar(dto: MarcaDTO): Observable<Marca> {
    return this.http.post<Marca>(this.baseUrl, dto);
  }

  atualizar(id: number, dto: MarcaDTO): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${id}`, dto);
  }

  /** Envia o logo (PNG ou SVG até 2 MB) e devolve o novo logoUrl (#84). */
  enviarLogo(id: number, arquivo: File): Observable<MarcaLogo> {
    const form = new FormData();
    form.set('arquivo', arquivo);
    return this.http.post<MarcaLogo>(`${this.baseUrl}/${id}/logo`, form);
  }

  /** logoUrl de arquivo enviado é relativo à API; URL externa fica como está. */
  urlLogo(logoUrl: string): string {
    return logoUrl.startsWith('/') ? `${environment.apiUrl}${logoUrl}` : logoUrl;
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
