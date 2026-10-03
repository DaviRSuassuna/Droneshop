import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Estado } from '../models/endereco.model';

@Injectable({ providedIn: 'root' })
export class EstadoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/estados`;

  listar(): Observable<Estado[]> {
    return this.http.get<Estado[]>(this.baseUrl);
  }
}
