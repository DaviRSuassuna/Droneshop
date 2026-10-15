import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { ProdutoDTO } from '../models/produto.model';
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
    service.listar({ nome: '   ', idMarca: null, idStatus: null }).subscribe();
    const req = http.expectOne((r) => r.url === url);
    const params = req.request.params;
    expect(params.keys().sort()).toEqual(['page', 'pageSize']);
    expect(params.get('pageSize')).toBe('10');
    req.flush({ items: [], page: 0, pageSize: 10, totalItems: 0, totalPages: 0 });
  });

  it('envia só os filtros preenchidos, com o nome sem espaços nas pontas', () => {
    service
      .listar({ nome: '  Mavic ', idMarca: 1, idCategoria: 2, idSubcategoria: 3, idStatus: 2 }, 2, 20)
      .subscribe();
    const req = http.expectOne((r) => r.url === url);
    const params = req.request.params;
    expect(params.get('nome')).toBe('Mavic');
    expect(params.get('idMarca')).toBe('1');
    expect(params.get('idCategoria')).toBe('2');
    expect(params.get('idSubcategoria')).toBe('3');
    expect(params.get('idStatus')).toBe('2');
    expect(params.get('page')).toBe('2');
    expect(params.get('pageSize')).toBe('20');
    req.flush({ items: [], page: 0, pageSize: 10, totalItems: 0, totalPages: 0 });
  });

  it('busca pelo slug', () => {
    service.buscarPorSlug('falcon-x2-pro').subscribe();
    const req = http.expectOne(`${url}/slug/falcon-x2-pro`);
    expect(req.request.method).toBe('GET');
    req.flush({ id: 1, slug: 'falcon-x2-pro' });
  });

  it('cria, atualiza e exclui', () => {
    const dto = { nome: 'Mavic', idStatus: 2 } as ProdutoDTO;
    service.criar(dto).subscribe();
    const post = http.expectOne(url);
    expect(post.request.method).toBe('POST');
    expect(post.request.body).toEqual(dto);
    post.flush({ id: 1 });

    service.atualizar(1, dto).subscribe();
    expect(http.expectOne(`${url}/1`).request.method).toBe('PUT');

    service.excluir(1).subscribe();
    expect(http.expectOne(`${url}/1`).request.method).toBe('DELETE');
  });
});
