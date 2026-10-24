import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { EstoqueService } from './estoque.service';

describe('EstoqueService', () => {
  const url = `${environment.apiUrl}/estoque`;
  const vazia = { items: [], page: 0, pageSize: 10, totalItems: 0, totalPages: 0 };
  let service: EstoqueService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(EstoqueService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista o saldo sem filtros (padrão 0 e 10)', () => {
    service.listar().subscribe((p) => expect(p.items).toEqual([]));
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.keys().sort()).toEqual(['page', 'pageSize']);
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('pageSize')).toBe('10');
    req.flush(vazia);
  });

  it('envia situação, busca e marca; TODOS e busca vazia não vão para a API', () => {
    service
      .listar({ situacao: 'ABAIXO_MINIMO', busca: '  DRN-FX2P ', idMarca: 4 }, 1, 20)
      .subscribe();
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.params.get('situacao')).toBe('ABAIXO_MINIMO');
    expect(req.request.params.get('idMarca')).toBe('4');
    expect(req.request.params.get('busca')).toBe('DRN-FX2P');
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('pageSize')).toBe('20');
    req.flush(vazia);

    service.listar({ situacao: 'TODOS', busca: '   ' }).subscribe();
    const req2 = http.expectOne((r) => r.url === url);
    expect(req2.request.params.keys().sort()).toEqual(['page', 'pageSize']);
    req2.flush(vazia);
  });

  it('lista movimentos filtrando por produto e tipo', () => {
    service.listarMovimentos({ idProduto: 31, idTipo: 2 }, 0, 5).subscribe();
    const req = http.expectOne((r) => r.url === `${url}/movimentos`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('idProduto')).toBe('31');
    expect(req.request.params.get('idTipo')).toBe('2');
    expect(req.request.params.get('pageSize')).toBe('5');
    req.flush(vazia);
  });

  it('não envia filtros vazios de movimento', () => {
    service.listarMovimentos({ idProduto: null, idTipo: null }).subscribe();
    const req = http.expectOne((r) => r.url === `${url}/movimentos`);
    expect(req.request.params.keys().sort()).toEqual(['page', 'pageSize']);
    req.flush(vazia);
  });

  it('registra um movimento', () => {
    const corpo = { idProduto: 31, idTipo: 1, quantidade: 5, observacao: 'NF 123' };
    service.registrarMovimento(corpo).subscribe((m) => expect(m.saldoResultante).toBe(15));
    const req = http.expectOne(`${url}/movimentos`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(corpo);
    req.flush({ id: 90, saldoResultante: 15 });
  });
  it('exporta todos os resultados com os filtros e resposta binária', () => {
    service.exportar({ busca: '  Falcon ', idMarca: 3, situacao: 'ZERADO' }).subscribe();
    const req = http.expectOne((r) => r.url === url + '/exportar');
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');
    expect(req.request.params.has('page')).toBe(false);
    expect(req.request.params.has('pageSize')).toBe(false);
    expect(req.request.params.get('busca')).toBe('Falcon');
    expect(req.request.params.get('idMarca')).toBe('3');
    expect(req.request.params.get('situacao')).toBe('ZERADO');
    req.flush(new Blob(['csv'], { type: 'text/csv' }));
  });
});
