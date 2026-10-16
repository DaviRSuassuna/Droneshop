import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ConfiguracaoLoja, ConfiguracaoLojaDTO } from '../models/configuracao-loja.model';

@Injectable({ providedIn: 'root' })
export class ConfiguracaoLojaService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/admin/configuracao`;

  buscar(): Observable<ConfiguracaoLoja> {
    return this.http.get<ConfiguracaoLoja>(this.url);
  }

  atualizar(dto: ConfiguracaoLojaDTO): Observable<void> {
    return this.http.put<void>(this.url, dto);
  }
}
