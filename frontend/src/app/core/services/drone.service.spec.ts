import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { DroneDTO } from '../models/produto.model';
import { DroneService } from './drone.service';

describe('DroneService', () => {
  const url = `${environment.apiUrl}/drones`;
  let service: DroneService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(DroneService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('cria com POST em /drones', () => {
    const dto = { nome: 'Exemplo', idStatus: 2 } as DroneDTO;
    service.criar(dto).subscribe();
    const req = http.expectOne(url);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
    req.flush({ id: 1 });
  });

  it('altera com PUT em /drones/{id}', () => {
    const dto = { nome: 'Exemplo', idStatus: 2 } as DroneDTO;
    service.atualizar(7, dto).subscribe();
    const req = http.expectOne(`${url}/7`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(dto);
    req.flush(null);
  });
});
