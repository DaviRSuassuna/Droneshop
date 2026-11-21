import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { EnderecoClienteService } from './endereco-cliente.service';
import { EnderecoClienteDTO } from '../models/cliente.model';

describe('EnderecoClienteService', () => {
  const url = `${environment.apiUrl}/conta/enderecos`;
  const dto: EnderecoClienteDTO = {
    apelido: 'Casa',
    cep: '77001-002',
    logradouro: 'Quadra 104 Norte',
    numero: '10',
    complemento: '',
    bairro: 'Plano Diretor Norte',
    idMunicipio: 1,
    principal: true,
  };
  let service: EnderecoClienteService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(EnderecoClienteService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista os endereços', () => {
    service.listar().subscribe();
    const req = http.expectOne(url);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('cria com POST e o corpo do contrato', () => {
    service.criar(dto).subscribe();
    const req = http.expectOne(url);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
    req.flush({ id: 5 });
  });

  it('altera com PUT /{id}', () => {
    service.alterar(5, dto).subscribe();
    const req = http.expectOne(`${url}/5`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(dto);
    req.flush(null, { status: 204, statusText: 'No Content' });
  });

  it('exclui com DELETE /{id}', () => {
    service.excluir(5).subscribe();
    const req = http.expectOne(`${url}/5`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null, { status: 204, statusText: 'No Content' });
  });

  it('marca como principal com PATCH /{id}/principal', () => {
    service.marcarPrincipal(5).subscribe();
    const req = http.expectOne(`${url}/5/principal`);
    expect(req.request.method).toBe('PATCH');
    req.flush(null, { status: 204, statusText: 'No Content' });
  });
});
