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

  it('altera o status com PATCH e observação sem espaços', () => {
    let concluiu = false;
    service.alterarStatus(7, 4, '  Postado nos Correios ').subscribe(() => (concluiu = true));
    const req = http.expectOne(`${url}/7/status`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ idStatus: 4, observacao: 'Postado nos Correios' });
    req.flush(null, { status: 204, statusText: 'No Content' });
    expect(concluiu).toBe(true);
  });

  it('manda observação nula quando vazia', () => {
    service.alterarStatus(7, 8, '   ').subscribe();
    const req = http.expectOne(`${url}/7/status`);
    expect(req.request.body).toEqual({ idStatus: 8, observacao: null });
    req.flush(null, { status: 204, statusText: 'No Content' });
  });

  it('repassa o 409 da transição não permitida', () => {
    let status = 0;
    service.alterarStatus(7, 2).subscribe({ error: (e) => (status = e.status) });
    http
      .expectOne(`${url}/7/status`)
      .flush(
        { type: '/errors/conflict', detail: 'O pedido não pode passar de ENTREGUE para PAGO.' },
        { status: 409, statusText: 'Conflict' },
      );
    expect(status).toBe(409);
  });

  it('busca o histórico do pedido', () => {
    service.historico(7).subscribe((h) => expect(h[0].statusNovo.nome).toBe('PENDENTE'));
    const req = http.expectOne(`${url}/7/historico`);
    expect(req.request.method).toBe('GET');
    req.flush([
      {
        statusAnterior: null,
        statusNovo: { id: 1, nome: 'PENDENTE' },
        origem: { id: 2, nome: 'SISTEMA' },
        autor: null,
        observacao: null,
        dataCadastro: '2026-10-01T10:00:00',
      },
    ]);
  });
  it('exporta todos os resultados com os filtros e resposta binária', () => {
    service
      .exportar({ busca: '  AER ', idStatus: 2, dataInicio: '2026-10-01', dataFim: '2026-10-31' })
      .subscribe();
    const req = http.expectOne((r) => r.url === url + '/exportar');
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');
    expect(req.request.params.has('page')).toBe(false);
    expect(req.request.params.has('pageSize')).toBe(false);
    expect(req.request.params.get('busca')).toBe('AER');
    expect(req.request.params.get('idStatus')).toBe('2');
    expect(req.request.params.get('dataInicio')).toBe('2026-10-01');
    expect(req.request.params.get('dataFim')).toBe('2026-10-31');
    req.flush(new Blob(['csv'], { type: 'text/csv' }));
  });
  it('envia o método de pagamento na listagem e na exportação', () => {
    service.listar({ idMetodoPagamento: 2 }).subscribe();
    const lista = http.expectOne((r) => r.url === url);
    expect(lista.request.params.get('idMetodoPagamento')).toBe('2');
    lista.flush({ items: [], page: 0, pageSize: 10, totalItems: 0, totalPages: 0 });
    service.exportar({ idMetodoPagamento: 1 }).subscribe();
    const csv = http.expectOne((r) => r.url === url + '/exportar');
    expect(csv.request.params.get('idMetodoPagamento')).toBe('1');
    csv.flush(new Blob(['csv']));
  });

  it('busca os indicadores do painel', () => {
    service.indicadores().subscribe();
    const req = http.expectOne(url + '/indicadores');
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('gera as etiquetas em lote por POST com os ids e resposta binária', () => {
    service.etiquetas([12, 15]).subscribe();
    const req = http.expectOne(url + '/etiquetas');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ ids: [12, 15] });
    expect(req.request.responseType).toBe('blob');
    req.flush(new Blob(['%PDF-1.4'], { type: 'application/pdf' }));
  });

  it('baixa a etiqueta PDF do pedido por GET', () => {
    service.etiqueta(7).subscribe();
    const req = http.expectOne(url + '/7/etiqueta');
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');
    req.flush(new Blob(['%PDF-1.4'], { type: 'application/pdf' }));
  });
});
