import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { AuditoriaService } from './auditoria.service';

describe('AuditoriaService', () => {
  const api = environment.apiUrl;
  let service: AuditoriaService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(AuditoriaService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista as ações de auditoria', () => {
    service.acoes().subscribe();
    const req = http.expectOne(`${api}/acoes-auditoria`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('lista o log com filtros, sem mandar os vazios', () => {
    service.listar({ entidade: 'Produto', entidadeId: '31', idAcao: 2, idUsuario: null }, 1, 50).subscribe();
    const req = http.expectOne((r) => r.url === `${api}/admin/auditoria`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('entidade')).toBe('Produto');
    expect(req.request.params.get('entidadeId')).toBe('31');
    expect(req.request.params.get('idAcao')).toBe('2');
    expect(req.request.params.has('idUsuario')).toBe(false);
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('pageSize')).toBe('50');
    req.flush({ items: [], page: 1, pageSize: 50, totalItems: 0, totalPages: 0 });
  });
});
