import { STATUS_PRODUTO } from '../models/status.model';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { ProdutoService } from './produto.service';

describe('ProdutoService', () => {
  const url = `${environment.apiUrl}/produtos`;
  let service: ProdutoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ProdutoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista com paginação padrão de 10 e sem filtros vazios', () => {
    service
      .listar({ idTipo: null, busca: '   ', idMarca: null, idStatus: null, kit: null })
      .subscribe();
    const req = http.expectOne((r) => r.url === url);
    const params = req.request.params;
    expect(params.keys().sort()).toEqual(['page', 'pageSize']);
    expect(params.get('pageSize')).toBe('10');
    req.flush({ items: [], page: 0, pageSize: 10, totalItems: 0, totalPages: 0 });
  });

  it('envia só os filtros preenchidos, com a busca sem espaços nas pontas', () => {
    service
      .listar(
        {
          idTipo: 1,
          busca: '  Mavic ',
          idMarca: 1,
          idCategoria: 2,
          idSubcategoria: 3,
          idStatus: 2,
          idGrupo: 4,
          estoque: 'ZERADO',
        },
        2,
        20,
      )
      .subscribe();
    const req = http.expectOne((r) => r.url === url);
    const params = req.request.params;
    expect(params.get('idTipo')).toBe('1');
    expect(params.get('busca')).toBe('Mavic');
    expect(params.get('idMarca')).toBe('1');
    expect(params.get('idCategoria')).toBe('2');
    expect(params.get('idSubcategoria')).toBe('3');
    expect(params.get('idStatus')).toBe('2');
    expect(params.get('idGrupo')).toBe('4');
    expect(params.get('estoque')).toBe('ZERADO');
    expect(params.get('page')).toBe('2');
    expect(params.get('pageSize')).toBe('20');
    req.flush({ items: [], page: 0, pageSize: 10, totalItems: 0, totalPages: 0 });
  });

  it('select de produto: idStatus=2 e kit=false (D40)', () => {
    service.listar({ busca: 'DRN', idStatus: STATUS_PRODUTO.ATIVO, kit: false }).subscribe();
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.params.get('busca')).toBe('DRN');
    expect(req.request.params.get('idStatus')).toBe(String(STATUS_PRODUTO.ATIVO));
    expect(req.request.params.get('kit')).toBe('false');
    req.flush({ items: [], page: 0, pageSize: 10, totalItems: 0, totalPages: 0 });
  });

  it('kit=true não vira filtro (para listar só kits use idTipo)', () => {
    service.listar({ kit: true }).subscribe();
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.params.has('kit')).toBe(false);
    req.flush({ items: [], page: 0, pageSize: 10, totalItems: 0, totalPages: 0 });
  });

  it('busca pelo slug', () => {
    service.buscarPorSlug('falcon-x2-pro').subscribe();
    const req = http.expectOne(`${url}/slug/falcon-x2-pro`);
    expect(req.request.method).toBe('GET');
    req.flush({ id: 1, slug: 'falcon-x2-pro' });
  });

  it('exclui pelo id', () => {
    service.excluir(1).subscribe();
    expect(http.expectOne(`${url}/1`).request.method).toBe('DELETE');
  });
  it('exporta todos os resultados com os filtros e resposta binária', () => {
    service.exportar({ busca: '  Falcon ', idMarca: 3, idStatus: 2 }).subscribe();
    const req = http.expectOne((r) => r.url === url + '/exportar');
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');
    expect(req.request.params.has('page')).toBe(false);
    expect(req.request.params.has('pageSize')).toBe(false);
    expect(req.request.params.get('busca')).toBe('Falcon');
    expect(req.request.params.get('idMarca')).toBe('3');
    expect(req.request.params.get('idStatus')).toBe('2');
    req.flush(new Blob(['csv'], { type: 'text/csv' }));
  });

  it('envia ids e parâmetros da ação em lote por PATCH', () => {
    const dto = { ids: [1, 3], acao: 'REAJUSTAR_PRECO' as const, percentual: 5 };
    service
      .alterarEmLote(dto)
      .subscribe((res) => expect(res.falhas).toEqual([{ id: 3, motivo: 'Sem preço' }]));
    const req = http.expectOne(url + '/lote');
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual(dto);
    req.flush({ processados: 1, falhas: [{ id: 3, motivo: 'Sem preço' }] });
  });
});
