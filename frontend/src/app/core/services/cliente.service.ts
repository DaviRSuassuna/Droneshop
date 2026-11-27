import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PerfilCliente, PerfilDTO, SenhaDTO } from '../models/cliente.model';
import { StatusRef } from '../models/status.model';

/** Perfil do cliente logado (#19). */
@Injectable({ providedIn: 'root' })
export class ClienteService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  tiposPessoa(): Observable<StatusRef[]> {
    return this.http.get<StatusRef[]>(`${this.api}/tipos-pessoa`);
  }

  perfil(): Observable<PerfilCliente> {
    return this.http.get<PerfilCliente>(`${this.api}/conta/perfil`);
  }

  alterarPerfil(perfil: PerfilDTO): Observable<void> {
    return this.http.put<void>(`${this.api}/conta/perfil`, perfil);
  }

  trocarSenha(senha: SenhaDTO): Observable<void> {
    return this.http.put<void>(`${this.api}/conta/senha`, senha);
  }
}
