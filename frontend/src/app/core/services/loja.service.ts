import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Comparacao } from '../models/comparacao.model';
import { environment } from '../../../environments/environment';
import {
  CategoriaLoja,
  ConfiguracaoLojaPublica,
  FiltroProdutosLoja,
  FiltrosLoja,
  MarcaLoja,
  OfertaLoja,
  ProdutoLojaDetalhe,
  ProdutoLojaItem,
  ProdutoLojaResumo,
} from '../models/loja.model';
import { PageResponse } from '../models/page.model';

/** Rotas públicas da vitrine (#21). */
@Injectable({ providedIn: 'root' })
export class LojaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/loja`;

  listarCategorias(tipo?: 'DRONE' | 'ACESSORIO'): Observable<CategoriaLoja[]> {
    const params = tipo ? new HttpParams().set('tipo', tipo) : new HttpParams();
    return this.http.get<CategoriaLoja[]>(`${this.baseUrl}/categorias`, { params });
  }

  listarMarcas(destaque = false): Observable<MarcaLoja[]> {
    const params = destaque ? new HttpParams().set('destaque', true) : new HttpParams();
    return this.http.get<MarcaLoja[]>(`${this.baseUrl}/marcas`, { params });
  }

  /** Filtros vazios não vão na URL. */
  listarProdutos(
    filtro: FiltroProdutosLoja = {},
    page = 0,
    pageSize = 12,
  ): Observable<PageResponse<ProdutoLojaItem>> {
    let params = new HttpParams().set('page', page).set('pageSize', pageSize);
    for (const [chave, valor] of Object.entries(filtro)) {
      if (valor === null || valor === undefined || `${valor}`.trim() === '') continue;
      params = params.set(chave, `${valor}`.trim());
    }
    return this.http.get<PageResponse<ProdutoLojaItem>>(`${this.baseUrl}/produtos`, { params });
  }

  /** Barra de busca (#43): nome, SKU, marca e tags; a API grava o termo. */
  buscar(q: string, page = 0, pageSize = 12): Observable<PageResponse<ProdutoLojaItem>> {
    const params = new HttpParams().set('q', q.trim()).set('page', page).set('pageSize', pageSize);
    return this.http.get<PageResponse<ProdutoLojaItem>>(`${this.baseUrl}/busca`, { params });
  }

  /** Comparação de 2 a 4 drones (#44): versões do grupo e/ou ids avulsos. */
  comparar(grupo: string | null, ids: number[] = []): Observable<Comparacao> {
    let params = new HttpParams();
    if (grupo) params = params.set('grupo', grupo);
    if (ids.length) params = params.set('ids', ids.join(','));
    return this.http.get<Comparacao>(`${this.baseUrl}/comparar`, { params });
  }

  buscarFiltros(categoria?: string): Observable<FiltrosLoja> {
    const params = categoria ? new HttpParams().set('categoria', categoria) : new HttpParams();
    return this.http.get<FiltrosLoja>(`${this.baseUrl}/filtros`, { params });
  }

  buscarProduto(slug: string): Observable<ProdutoLojaDetalhe> {
    return this.http.get<ProdutoLojaDetalhe>(
      `${this.baseUrl}/produtos/${encodeURIComponent(slug)}`,
    );
  }

  listarCompativeis(slug: string): Observable<ProdutoLojaResumo[]> {
    return this.http.get<ProdutoLojaResumo[]>(
      `${this.baseUrl}/produtos/${encodeURIComponent(slug)}/compativeis`,
    );
  }

  listarOfertas(): Observable<OfertaLoja[]> {
    return this.http.get<OfertaLoja[]>(`${this.baseUrl}/ofertas`);
  }

  buscarConfiguracao(): Observable<ConfiguracaoLojaPublica> {
    return this.http.get<ConfiguracaoLojaPublica>(`${this.baseUrl}/configuracao`);
  }
}
