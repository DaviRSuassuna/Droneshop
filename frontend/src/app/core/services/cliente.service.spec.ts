import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { ClienteService } from './cliente.service';

describe('ClienteService', () => {
  const api = environment.apiUrl;
  let service: ClienteService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ClienteService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista os tipos de pessoa', () => {
    service.tiposPessoa().subscribe();
    const req = http.expectOne(`${api}/tipos-pessoa`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('lê e altera o perfil do cliente logado', () => {
    service.perfil().subscribe();
    const get = http.expectOne(`${api}/conta/perfil`);
    expect(get.request.method).toBe('GET');
    get.flush({});

    service.alterarPerfil({ nome: 'Novo', telefone: '63988887777' }).subscribe();
    const put = http.expectOne(`${api}/conta/perfil`);
    expect(put.request.method).toBe('PUT');
    expect(put.request.body).toEqual({ nome: 'Novo', telefone: '63988887777' });
    put.flush(null, { status: 204, statusText: 'No Content' });
  });

  it('troca a senha com PUT /conta/senha', () => {
    service.trocarSenha({ senhaAtual: 'antiga123', senhaNova: 'nova12345' }).subscribe();
    const req = http.expectOne(`${api}/conta/senha`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ senhaAtual: 'antiga123', senhaNova: 'nova12345' });
    req.flush(null, { status: 204, statusText: 'No Content' });
  });
});
