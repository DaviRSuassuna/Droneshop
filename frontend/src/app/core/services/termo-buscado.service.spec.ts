import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { TermoBuscadoService } from './termo-buscado.service';

describe('TermoBuscadoService', () => {
  const url = `${environment.apiUrl}/admin/termos-buscados`;
  let service: TermoBuscadoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(TermoBuscadoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista com filtros e paginação', () => {
    service.listar({ semResultado: true, dataInicio: '2026-01-01', dataFim: '2026-01-31' }, 1, 50).subscribe();
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('semResultado')).toBe('true');
    expect(req.request.params.get('dataInicio')).toBe('2026-01-01');
    expect(req.request.params.get('dataFim')).toBe('2026-01-31');
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('pageSize')).toBe('50');
    req.flush({ items: [], page: 1, pageSize: 50, totalItems: 0, totalPages: 0 });
  });

  it('não manda filtro vazio', () => {
    service.listar({}).subscribe();
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.params.has('semResultado')).toBe(false);
    expect(req.request.params.has('dataInicio')).toBe(false);
    req.flush({ items: [], page: 0, pageSize: 20, totalItems: 0, totalPages: 0 });
  });
});
