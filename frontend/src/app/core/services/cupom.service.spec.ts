import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { CupomDTO } from '../models/cupom.model';
import { CupomService } from './cupom.service';

describe('CupomService', () => {
  const url = `${environment.apiUrl}/cupons`;
  let service: CupomService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(CupomService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista com paginação e filtros opcionais', () => {
    service.listar('VIGENTE', 'piloto', 1, 20).subscribe();
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('situacao')).toBe('VIGENTE');
    expect(req.request.params.get('codigo')).toBe('piloto');
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('pageSize')).toBe('20');
    req.flush({ items: [], page: 1, pageSize: 20, totalItems: 0, totalPages: 0 });
  });

  it('cria, busca, atualiza, exclui e lista usos', () => {
    const dto: CupomDTO = {
      codigo: 'PILOTO10',
      idTipoDesconto: 1,
      valor: 10,
      dataInicio: '2026-08-01T00:00:00',
      dataFim: null,
      valorMinimo: null,
      limiteTotalUso: null,
      limitePorCliente: 1,
      ativo: true,
      idsCategorias: [],
      idsMarcas: [],
      idsVariantes: [],
    };

    service.criar(dto).subscribe();
    const post = http.expectOne(url);
    expect(post.request.method).toBe('POST');
    expect(post.request.body).toEqual(dto);
    post.flush({ id: 3 });

    service.buscarPorId(3).subscribe();
    http.expectOne(`${url}/3`).flush({ id: 3 });

    service.atualizar(3, dto).subscribe();
    const put = http.expectOne(`${url}/3`);
    expect(put.request.method).toBe('PUT');
    put.flush(null);

    service.usos(3).subscribe();
    const usos = http.expectOne((r) => r.url === `${url}/3/usos`);
    expect(usos.request.params.get('pageSize')).toBe('1000');
    usos.flush({ items: [], page: 0, pageSize: 1000, totalItems: 0, totalPages: 0 });

    service.excluir(3).subscribe();
    const del = http.expectOne(`${url}/3`);
    expect(del.request.method).toBe('DELETE');
    del.flush(null);
  });
});
