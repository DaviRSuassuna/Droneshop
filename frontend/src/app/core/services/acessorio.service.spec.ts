import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { AcessorioDTO } from '../models/produto.model';
import { AcessorioService } from './acessorio.service';

describe('AcessorioService', () => {
  const url = `${environment.apiUrl}/acessorios`;
  let service: AcessorioService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AcessorioService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('cria com POST em /acessorios', () => {
    const dto = { nome: 'Exemplo', idStatus: 2, idsCompativeis: [10, 12] } as AcessorioDTO;
    service.criar(dto).subscribe();
    const req = http.expectOne(url);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
    req.flush({ id: 1 });
  });

  it('envia peso do item e sugestão nos compatíveis no corpo (#84)', () => {
    const dto = { nome: 'Bateria', idStatus: 1, pesoItemG: 85, sugerirNosCompativeis: true } as AcessorioDTO;
    service.criar(dto).subscribe();
    const req = http.expectOne(url);
    expect(req.request.body.pesoItemG).toBe(85);
    expect(req.request.body.sugerirNosCompativeis).toBe(true);
    req.flush({ id: 1 });
  });

  it('altera com PUT em /acessorios/{id}', () => {
    const dto = { nome: 'Exemplo', idStatus: 2, idsCompativeis: [10, 12] } as AcessorioDTO;
    service.atualizar(7, dto).subscribe();
    const req = http.expectOne(`${url}/7`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(dto);
    req.flush(null);
  });
});
