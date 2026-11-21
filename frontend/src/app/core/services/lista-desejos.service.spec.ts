import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { ListaDesejosService } from './lista-desejos.service';

describe('ListaDesejosService', () => {
  const url = `${environment.apiUrl}/conta/desejos`;
  let service: ListaDesejosService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(ListaDesejosService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista com GET /conta/desejos', () => {
    service.listar().subscribe();
    const req = http.expectOne(url);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('adiciona com POST e idProduto no corpo', () => {
    service.adicionar(31).subscribe();
    const req = http.expectOne(url);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ idProduto: 31 });
    req.flush(null);
  });

  it('remove com DELETE /conta/desejos/{idProduto}', () => {
    service.remover(31).subscribe();
    const req = http.expectOne(`${url}/31`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
