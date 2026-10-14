import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { EstadoService } from './estado.service';

describe('EstadoService', () => {
  let service: EstadoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(EstadoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista os 27 estados numa página só', () => {
    service.listar().subscribe((p) => expect(p.items.length).toBe(1));
    const req = http.expectOne((r) => r.url === `${environment.apiUrl}/estados`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('pageSize')).toBe('27');
    req.flush({
      items: [{ id: 1, nome: 'Tocantins', sigla: 'TO', codigoIbge: 17 }],
      page: 0,
      pageSize: 27,
      totalItems: 1,
      totalPages: 1,
    });
  });
});
