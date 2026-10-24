import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { MarcaDTO } from '../models/marca.model';
import { MarcaService } from './marca.service';

describe('MarcaService', () => {
  const url = `${environment.apiUrl}/marcas`;
  const dto = { nome: 'Aerion', idStatus: 2 } as MarcaDTO;
  let service: MarcaService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(MarcaService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista uma página (padrão 0 e 10)', () => {
    service.listar().subscribe((p) => expect(p.items).toEqual([]));
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('pageSize')).toBe('10');
    req.flush({ items: [], page: 0, pageSize: 10, totalItems: 0, totalPages: 0 });
  });

  it('busca por nome com o termo codificado na rota', () => {
    service.buscarPorNome('Aerion X').subscribe();
    const req = http.expectOne((r) => r.url === `${url}/nome/Aerion%20X`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('pageSize')).toBe('10');
    req.flush({ items: [], page: 0, pageSize: 10, totalItems: 0, totalPages: 0 });
  });

  it('busca por id', () => {
    service.buscarPorId(7).subscribe();
    const req = http.expectOne(`${url}/7`);
    expect(req.request.method).toBe('GET');
    req.flush({ id: 7 });
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
  it('exporta todos os resultados com os filtros e resposta binária', () => {
    service.exportar({ busca: '  Aerion ', idStatus: 2, paisOrigem: 'Brasil' }).subscribe();
    const req = http.expectOne((r) => r.url === url + '/exportar');
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');
    expect(req.request.params.has('page')).toBe(false);
    expect(req.request.params.has('pageSize')).toBe(false);
    expect(req.request.params.get('busca')).toBe('Aerion');
    expect(req.request.params.get('idStatus')).toBe('2');
    expect(req.request.params.get('paisOrigem')).toBe('Brasil');
    req.flush(new Blob(['csv'], { type: 'text/csv' }));
  });
});
