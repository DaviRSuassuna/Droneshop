import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { SubcategoriaDTO } from '../models/subcategoria.model';
import { SubcategoriaService } from './subcategoria.service';

describe('SubcategoriaService', () => {
  const url = `${environment.apiUrl}/subcategorias`;
  const dto = { nome: 'Baterias', categoriaId: 2, status: 'ATIVA' } as SubcategoriaDTO;
  let service: SubcategoriaService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(SubcategoriaService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista todas quando não recebe a categoria', () => {
    service.listar().subscribe();
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.params.has('categoriaId')).toBe(false);
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('pageSize')).toBe('500');
    req.flush([]);
  });

  it('filtra pela categoria quando recebe o id', () => {
    service.listar(2).subscribe();
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.params.get('categoriaId')).toBe('2');
    req.flush([]);
  });

  it('ignora categoria null', () => {
    service.listar(null).subscribe();
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.params.has('categoriaId')).toBe(false);
    req.flush([]);
  });

  it('busca por id', () => {
    service.buscarPorId(4).subscribe();
    expect(http.expectOne(`${url}/4`).request.method).toBe('GET');
  });

  it('cria, atualiza e exclui', () => {
    service.criar(dto).subscribe();
    const post = http.expectOne(url);
    expect(post.request.method).toBe('POST');
    expect(post.request.body).toEqual(dto);
    post.flush({ id: 1 });

    service.atualizar(1, dto).subscribe();
    const put = http.expectOne(`${url}/1`);
    expect(put.request.method).toBe('PUT');
    put.flush(null);

    service.excluir(1).subscribe();
    const del = http.expectOne(`${url}/1`);
    expect(del.request.method).toBe('DELETE');
    del.flush(null);
  });
});
