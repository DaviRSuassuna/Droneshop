import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { DashboardService } from './dashboard.service';

describe('DashboardService', () => {
  const url = `${environment.apiUrl}/admin/dashboard`;
  let service: DashboardService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(DashboardService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('busca sem datas (a API usa os últimos 30 dias)', () => {
    service.buscar().subscribe((d) => expect(d.quantidadePedidos).toBe(2));
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.keys()).toEqual([]);
    req.flush({
      periodo: { dataInicio: '2026-09-17', dataFim: '2026-10-17' },
      faturamento: 100.0,
      quantidadePedidos: 2,
      ticketMedio: 50.0,
      pedidosPorStatus: [],
      estoqueBaixo: [],
      ultimosPedidos: [],
    });
  });

  it('envia o período para a API', () => {
    service.buscar({ dataInicio: '2026-01-01', dataFim: '2026-10-17' }).subscribe();
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.params.get('dataInicio')).toBe('2026-01-01');
    expect(req.request.params.get('dataFim')).toBe('2026-10-17');
    req.flush({});
  });

  it('não envia datas vazias', () => {
    service.buscar({ dataInicio: '', dataFim: null }).subscribe();
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.params.keys()).toEqual([]);
    req.flush({});
  });
});
