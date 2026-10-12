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
    service.listar({ nome: '   ', marcaId: null, status: '' }).subscribe();
    const req = http.expectOne((r) => r.url === url);
    const params = req.request.params;
    expect(params.keys().sort()).toEqual(['page', 'pageSize']);
    expect(params.get('pageSize')).toBe('10');
    req.flush([]);
  });

  it('envia só os filtros preenchidos, com o nome sem espaços nas pontas', () => {
    service
      .listar({ nome: '  Mavic ', marcaId: 1, categoriaId: 2, subcategoriaId: 3, status: 'ATIVO' }, 2, 20)
      .subscribe();
    const req = http.expectOne((r) => r.url === url);
    const params = req.request.params;
    expect(params.get('nome')).toBe('Mavic');
    expect(params.get('marcaId')).toBe('1');
    expect(params.get('categoriaId')).toBe('2');
    expect(params.get('subcategoriaId')).toBe('3');
    expect(params.get('status')).toBe('ATIVO');
    expect(params.get('page')).toBe('2');
    expect(params.get('pageSize')).toBe('20');
    req.flush([]);
  });

  it('conta com os mesmos filtros e sem paginação', () => {
    service.contar({ marcaId: 1 }).subscribe((total) => expect(total).toBe(4));
    const req = http.expectOne((r) => r.url === `${url}/count`);
    expect(req.request.params.get('marcaId')).toBe('1');
    expect(req.request.params.has('page')).toBe(false);
    req.flush(4);
  });

  it('cria, atualiza e exclui', () => {
    const dto = { nome: 'Mavic', status: 'ATIVO' } as ProdutoDTO;
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
