import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { CepService } from './cep.service';

describe('CepService', () => {
  const url = `${environment.apiUrl}/ceps`;
  let service: CepService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CepService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('consulta a API (não o ViaCEP) só com os dígitos do CEP', () => {
    service.buscar('77001-002').subscribe((r) => expect(r.cep).toBe('77001002'));
    const req = http.expectOne(`${url}/77001002`);
    expect(req.request.method).toBe('GET');
    req.flush({ cep: '77001002', municipio: null });
  });

  it('repassa o 503 para a tela seguir com preenchimento manual', () => {
    let status = 0;
    service.buscar('77001002').subscribe({ error: (e) => (status = e.status) });
    http.expectOne(`${url}/77001002`).flush(null, { status: 503, statusText: 'Service Unavailable' });
    expect(status).toBe(503);
  });
});
