import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { CategoriaDTO } from '../models/categoria.model';
import { CategoriaService } from './categoria.service';

describe('CategoriaService', () => {
  const url = `${environment.apiUrl}/categorias`;
  const dto = { nome: 'Drones', status: 'ATIVA' } as CategoriaDTO;
  let service: CategoriaService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CategoriaService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista com page e pageSize (padrão 0 e 500)', () => {
    service.listar().subscribe((itens) => expect(itens).toEqual([]));
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('pageSize')).toBe('500');
    req.flush([]);
  });

  it('busca por id', () => {
    service.buscarPorId(7).subscribe();
    const req = http.expectOne(`${url}/7`);
    expect(req.request.method).toBe('GET');
    req.flush({ id: 7 });
  });

  it('conta os registros', () => {
    service.contar().subscribe((total) => expect(total).toBe(3));
    const req = http.expectOne(`${url}/count`);
    expect(req.request.method).toBe('GET');
    req.flush(3);
  });

  it('cria com POST e o DTO no corpo', () => {
    service.criar(dto).subscribe();
    const req = http.expectOne(url);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
    req.flush({ id: 1 });
  });

  it('atualiza com PUT no id', () => {
    service.atualizar(5, dto).subscribe();
    const req = http.expectOne(`${url}/5`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(dto);
    req.flush(null);
  });

  it('exclui com DELETE no id', () => {
    service.excluir(5).subscribe();
    const req = http.expectOne(`${url}/5`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
