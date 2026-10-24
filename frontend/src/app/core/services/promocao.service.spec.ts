import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { PromocaoDTO } from '../models/promocao.model';
import { PromocaoService } from './promocao.service';

describe('PromocaoService', () => {
  const url = `${environment.apiUrl}/promocoes`;
  let service: PromocaoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(PromocaoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista promoções com paginação sem filtro de situação vazio', () => {
    service.listar().subscribe((pagina) => expect(pagina.items).toEqual([]));
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('pageSize')).toBe('10');
    expect(req.request.params.has('situacao')).toBe(false);
    req.flush({ items: [], page: 0, pageSize: 10, totalItems: 0, totalPages: 0 });
  });

  it('envia o filtro de situação e a paginação', () => {
    service.listar(' VIGENTE ', 2, 25).subscribe();
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.params.get('situacao')).toBe('VIGENTE');
    expect(req.request.params.get('page')).toBe('2');
    expect(req.request.params.get('pageSize')).toBe('25');
    req.flush({ items: [], page: 2, pageSize: 25, totalItems: 0, totalPages: 0 });
  });

  it('busca por id', () => {
    service.buscarPorId(7).subscribe((promocao) => expect(promocao.id).toBe(7));
    const req = http.expectOne(`${url}/7`);
    expect(req.request.method).toBe('GET');
    req.flush({ id: 7 });
  });

  it('cria, atualiza e exclui usando os endpoints da API', () => {
    const dto: PromocaoDTO = {
      nome: 'Semana do piloto',
      idEscopo: 1,
      dataInicio: '2026-08-24T00:00:00',
      dataFim: '2026-08-31T23:59:00',
      percentualDesconto: 22,
      valorDesconto: null,
      valorMinimoCarrinho: null,
      ativa: true,
      idsProdutos: [3],
    };

    service.criar(dto).subscribe();
    const post = http.expectOne(url);
    expect(post.request.method).toBe('POST');
    expect(post.request.body).toEqual(dto);
    post.flush({ id: 7 });

    service.atualizar(7, dto).subscribe();
    const put = http.expectOne(`${url}/7`);
    expect(put.request.method).toBe('PUT');
    expect(put.request.body).toEqual(dto);
    put.flush(null);

    service.excluir(7).subscribe();
    const del = http.expectOne(`${url}/7`);
    expect(del.request.method).toBe('DELETE');
    del.flush(null);
  });
});
