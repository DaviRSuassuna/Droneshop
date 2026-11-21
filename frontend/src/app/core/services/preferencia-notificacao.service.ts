import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PreferenciaNotificacao } from '../models/notificacao.model';

/** Preferências de notificação e descadastro (#42, RF41, RN10). */
@Injectable({ providedIn: 'root' })
export class PreferenciaNotificacaoService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  listar(): Observable<PreferenciaNotificacao[]> {
    return this.http.get<PreferenciaNotificacao[]>(`${this.api}/conta/preferencias-notificacao`);
  }

  salvar(preferencias: PreferenciaNotificacao[]): Observable<void> {
    return this.http.put<void>(`${this.api}/conta/preferencias-notificacao`, preferencias);
  }

  descadastrar(token: string): Observable<PreferenciaNotificacao> {
    return this.http.get<PreferenciaNotificacao>(
      `${this.api}/descadastro/${encodeURIComponent(token)}`,
    );
  }
}
