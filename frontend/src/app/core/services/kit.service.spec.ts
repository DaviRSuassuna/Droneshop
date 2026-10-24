import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { KitDTO } from '../models/produto.model';
import { KitService } from './kit.service';

describe('KitService', () => {
  const url = `${environment.apiUrl}/kits`;
  let service: KitService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(KitService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('cria com POST em /kits', () => {
    const dto = { nome: 'Combo', idStatus: 2, componentes: [{ idProduto: 1, quantidade: 2 }] } as KitDTO;
    service.criar(dto).subscribe();
    const req = http.expectOne(url);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
    req.flush({ id: 1 });
  });

  it('altera com PUT em /kits/{id}', () => {
    const dto = { nome: 'Combo', idStatus: 2, componentes: [{ idProduto: 1, quantidade: 2 }] } as KitDTO;
    service.atualizar(7, dto).subscribe();
    const req = http.expectOne(`${url}/7`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(dto);
    req.flush(null);
  });
});
