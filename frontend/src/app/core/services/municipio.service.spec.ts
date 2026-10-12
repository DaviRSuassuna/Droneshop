import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { MunicipioService } from './municipio.service';

describe('MunicipioService', () => {
  let service: MunicipioService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(MunicipioService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista os municípios do estado informado', () => {
    service.listarPorEstado(17).subscribe();
    const req = http.expectOne((r) => r.url === `${environment.apiUrl}/municipios`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('idEstado')).toBe('17');
    req.flush([]);
  });
});
