import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { VarianteService } from './variante.service';

describe('VarianteService', () => {
  const url = `${environment.apiUrl}/variantes`;
  let service: VarianteService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(VarianteService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista com paginação e filtros informados', () => {
    service.listar({ busca: ' fly ', ativa: true }, 0, 1000).subscribe();
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('pageSize')).toBe('1000');
    expect(req.request.params.get('busca')).toBe('fly');
    expect(req.request.params.get('ativa')).toBe('true');
    req.flush({ items: [], page: 0, pageSize: 1000, totalItems: 0, totalPages: 0 });
  });

  it('não manda filtros vazios', () => {
    service.listar({ busca: '  ', ativa: null }).subscribe();
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.params.has('busca')).toBe(false);
    expect(req.request.params.has('ativa')).toBe(false);
    req.flush({ items: [], page: 0, pageSize: 10, totalItems: 0, totalPages: 0 });
  });

  it('busca uma variante por id', () => {
    service.buscarPorId(15).subscribe((v) => expect(v.disponivel).toBe(12));
    const req = http.expectOne(`${url}/15`);
    expect(req.request.method).toBe('GET');
    req.flush({ id: 15, ehKit: true, disponivel: 12, componentes: [] });
  });
});
