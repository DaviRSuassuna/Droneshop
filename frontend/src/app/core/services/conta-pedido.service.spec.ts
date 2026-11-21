import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { ContaPedidoService } from './conta-pedido.service';

describe('ContaPedidoService', () => {
  const api = environment.apiUrl;
  let service: ContaPedidoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ContaPedidoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista os pedidos com page e pageSize', () => {
    service.listar(1, 5).subscribe();
    const req = http.expectOne((r) => r.url === `${api}/conta/pedidos`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('pageSize')).toBe('5');
    req.flush({ items: [], page: 1, pageSize: 5, totalItems: 0, totalPages: 0 });
  });

  it('busca pedido, rastreio e notas do cliente', () => {
    service.buscar(9).subscribe();
    expect(http.expectOne(`${api}/conta/pedidos/9`).request.method).toBe('GET');
    service.rastreio(9).subscribe();
    expect(http.expectOne(`${api}/conta/pedidos/9/rastreio`).request.method).toBe('GET');
    service.notas(9).subscribe();
    expect(http.expectOne(`${api}/conta/pedidos/9/nota-fiscal`).request.method).toBe('GET');
  });

  it('paga o pedido com o método escolhido', () => {
    service.pagar(9, { idMetodo: 1 }).subscribe();
    const req = http.expectOne(`${api}/pedidos/9/pagamentos`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ idMetodo: 1 });
  });

  it('lista e solicita devoluções', () => {
    service.devolucoes().subscribe();
    expect(http.expectOne(`${api}/conta/devolucoes`).request.method).toBe('GET');
    const dto = {
      idPedido: 9,
      idResolucao: 1,
      motivo: 'Arrependimento',
      itens: [{ idItemPedido: 15, quantidade: 1 }],
    };
    service.solicitarDevolucao(dto).subscribe();
    const req = http.expectOne(`${api}/conta/devolucoes`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
  });

  it('baixa o arquivo da nota pelo caminho devolvido', () => {
    service.arquivo('/notas-fiscais/3/danfe').subscribe();
    const req = http.expectOne(`${api}/notas-fiscais/3/danfe`);
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');
  });
});
