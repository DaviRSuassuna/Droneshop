import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  MidiaProduto,
  MidiaProdutoAlteracao,
  MidiaProdutoUpload,
  TipoMidia,
} from '../models/midia-produto.model';

@Injectable({ providedIn: 'root' })
export class MidiaProdutoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  listarTipos(): Observable<TipoMidia[]> {
    return this.http.get<TipoMidia[]>(`${this.baseUrl}/tipos-midia`);
  }

  listar(idProduto: number): Observable<MidiaProduto[]> {
    return this.http.get<MidiaProduto[]>(`${this.baseUrl}/produtos/${idProduto}/midias`);
  }

  adicionar(idProduto: number, dados: MidiaProdutoUpload): Observable<MidiaProduto> {
    const form = new FormData();
    form.set('idTipo', String(dados.idTipo));
    form.set('principal', String(dados.principal));
    if (dados.arquivo) form.set('arquivo', dados.arquivo);
    if (dados.url) form.set('url', dados.url);
    if (dados.ordem !== undefined) form.set('ordem', String(dados.ordem));
    if (dados.altText !== undefined) form.set('altText', dados.altText);
    return this.http.post<MidiaProduto>(`${this.baseUrl}/produtos/${idProduto}/midias`, form);
  }

  alterar(id: number, alteracao: MidiaProdutoAlteracao): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/midias/${id}`, alteracao);
  }

  reordenar(idProduto: number, ids: number[]): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/produtos/${idProduto}/midias/ordem`, { ids });
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/midias/${id}`);
  }

  urlArquivo(url: string): string {
    return `${this.baseUrl}${url}`;
  }
}
