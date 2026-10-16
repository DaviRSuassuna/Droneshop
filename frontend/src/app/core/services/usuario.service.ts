import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../models/page.model';
import {
  ModuloSistema,
  PapelAdministrativo,
  PermissaoModulo,
  UsuarioAdministrativo,
  UsuarioAdministrativoDTO,
} from '../models/usuario.model';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/admin/usuarios`;

  listar(
    nome = '',
    idPapel?: number,
    page = 0,
    pageSize = 10,
    ativo?: boolean,
  ): Observable<PageResponse<UsuarioAdministrativo>> {
    let params = new HttpParams().set('page', page).set('pageSize', pageSize);
    if (nome) {
      params = params.set('nome', nome);
    }
    if (idPapel) {
      params = params.set('idPapel', idPapel);
    }
    if (ativo !== undefined) {
      params = params.set('ativo', ativo);
    }
    return this.http.get<PageResponse<UsuarioAdministrativo>>(this.baseUrl, { params });
  }

  buscarPorId(id: number): Observable<UsuarioAdministrativo> {
    return this.http.get<UsuarioAdministrativo>(`${this.baseUrl}/${id}`);
  }

  criar(dto: UsuarioAdministrativoDTO): Observable<UsuarioAdministrativo> {
    return this.http.post<UsuarioAdministrativo>(this.baseUrl, dto);
  }

  atualizar(id: number, dto: UsuarioAdministrativoDTO): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${id}`, dto);
  }

  papeis(): Observable<PapelAdministrativo[]> {
    return this.http.get<PapelAdministrativo[]>(`${environment.apiUrl}/papeis-administrativos`);
  }

  modulos(): Observable<ModuloSistema[]> {
    return this.http.get<ModuloSistema[]>(`${environment.apiUrl}/modulos-sistema`);
  }

  permissoesPadrao(idPapel: number): Observable<PermissaoModulo[]> {
    return this.http.get<PermissaoModulo[]>(`${environment.apiUrl}/admin/papeis/${idPapel}/permissoes-padrao`);
  }
}
