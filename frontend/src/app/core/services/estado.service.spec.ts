import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { EstadoService } from './estado.service';

describe('EstadoService', () => {
  let service: EstadoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(EstadoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista os estados', () => {
    service.listar().subscribe((estados) => expect(estados.length).toBe(1));
    const req = http.expectOne(`${environment.apiUrl}/estados`);
    expect(req.request.method).toBe('GET');
    req.flush([{ id: 1, nome: 'Tocantins', sigla: 'TO', codigoIbge: 17 }]);
  });
});
