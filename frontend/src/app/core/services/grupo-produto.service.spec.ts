import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { GrupoProdutoDTO } from '../models/grupo-produto.model';
import { GrupoProdutoService } from './grupo-produto.service';

describe('GrupoProdutoService', () => {
  const url = `${environment.apiUrl}/grupos-produto`;
  let service: GrupoProdutoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(GrupoProdutoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista com paginação padrão e sem filtros vazios', () => {
    service.listar({ nome: '   ', ativo: null }).subscribe();
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.params.keys().sort()).toEqual(['page', 'pageSize']);
    expect(req.request.params.get('pageSize')).toBe('10');
    req.flush({ items: [], page: 0, pageSize: 10, totalItems: 0, totalPages: 0 });
  });

  it('filtra pelo nome sem espaços nas pontas e pelo status', () => {
    service.listar({ nome: '  Falcon ', ativo: false }, 1, 20).subscribe();
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.params.get('nome')).toBe('Falcon');
    expect(req.request.params.get('ativo')).toBe('false');
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('pageSize')).toBe('20');
    req.flush({ items: [], page: 1, pageSize: 20, totalItems: 0, totalPages: 0 });
  });

  it('busca pelo id', () => {
    service.buscarPorId(3).subscribe();
    expect(http.expectOne(`${url}/3`).request.method).toBe('GET');
  });

  it('cria com POST e altera com PUT', () => {
    const dto: GrupoProdutoDTO = { nome: 'Falcon X2', slug: '', descricaoCurta: '', ativo: true };
    service.criar(dto).subscribe();
    const post = http.expectOne(url);
    expect(post.request.method).toBe('POST');
    expect(post.request.body).toEqual(dto);
    post.flush({ id: 3 });

    service.atualizar(3, dto).subscribe();
    const put = http.expectOne(`${url}/3`);
    expect(put.request.method).toBe('PUT');
    expect(put.request.body).toEqual(dto);
    put.flush(null);
  });

  it('exclui pelo id', () => {
    service.excluir(3).subscribe();
    expect(http.expectOne(`${url}/3`).request.method).toBe('DELETE');
  });
});
