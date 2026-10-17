import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { PedidoService } from './pedido.service';

describe('PedidoService', () => {
  const url = `${environment.apiUrl}/pedidos`;
  let service: PedidoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(PedidoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista uma página sem filtros (padrão 0 e 10)', () => {
    service.listar().subscribe((p) => expect(p.items).toEqual([]));
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('pageSize')).toBe('10');
    expect(req.request.params.keys().sort()).toEqual(['page', 'pageSize']);
    req.flush({ items: [], page: 0, pageSize: 10, totalItems: 0, totalPages: 0 });
  });

  it('envia status, período e busca para a API', () => {
    service
      .listar(
        { idStatus: 4, dataInicio: '2026-10-01', dataFim: '2026-10-31', busca: '  AER-000012 ' },
        2,
        20,
      )
      .subscribe();
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.params.get('idStatus')).toBe('4');
    expect(req.request.params.get('dataInicio')).toBe('2026-10-01');
    expect(req.request.params.get('dataFim')).toBe('2026-10-31');
    expect(req.request.params.get('busca')).toBe('AER-000012');
    expect(req.request.params.get('page')).toBe('2');
    expect(req.request.params.get('pageSize')).toBe('20');
    req.flush({ items: [], page: 2, pageSize: 20, totalItems: 0, totalPages: 0 });
  });

  it('não envia filtros vazios', () => {
    service.listar({ idStatus: null, dataInicio: '', dataFim: null, busca: '   ' }).subscribe();
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.params.keys().sort()).toEqual(['page', 'pageSize']);
    req.flush({ items: [], page: 0, pageSize: 10, totalItems: 0, totalPages: 0 });
  });

  it('busca o resumo por status', () => {
    service.resumoStatus().subscribe((r) => expect(r.length).toBe(1));
    const req = http.expectOne(`${url}/resumo-status`);
    expect(req.request.method).toBe('GET');
    req.flush([{ status: { id: 1, nome: 'PENDENTE' }, quantidade: 3 }]);
  });

  it('busca por id', () => {
    service.buscarPorId(7).subscribe((p) => expect(p.numero).toBe('AER-000007'));
    const req = http.expectOne(`${url}/7`);
    expect(req.request.method).toBe('GET');
    req.flush({ id: 7, numero: 'AER-000007' });
  });
});
