import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { UsuarioAdministrativoDTO } from '../models/usuario.model';
import { UsuarioService } from './usuario.service';

describe('UsuarioService', () => {
  const url = `${environment.apiUrl}/admin/usuarios`;
  const dto = { nome: 'Marcos', email: 'marcos@aeria.com.br', idPapel: 1, ativo: true } as UsuarioAdministrativoDTO;
  let service: UsuarioService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(UsuarioService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista uma página (padrão 0 e 10)', () => {
    service.listar().subscribe((p) => expect(p.items).toEqual([]));
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('pageSize')).toBe('10');
    req.flush({ items: [], page: 0, pageSize: 10, totalItems: 0, totalPages: 0 });
  });

  it('envia filtros de nome, papel e status para a API', () => {
    service.listar('Marcos', 3, 1, 10, false).subscribe();
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.params.get('nome')).toBe('Marcos');
    expect(req.request.params.get('idPapel')).toBe('3');
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('ativo')).toBe('false');
    req.flush({ items: [], page: 1, pageSize: 10, totalItems: 0, totalPages: 0 });
  });

  it('busca por id', () => {
    service.buscarPorId(7).subscribe();
    const req = http.expectOne(`${url}/7`);
    expect(req.request.method).toBe('GET');
    req.flush({ id: 7 });
  });

  it('cria com POST e o DTO no corpo', () => {
    service.criar(dto).subscribe();
    const req = http.expectOne(url);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
    req.flush({ id: 1 });
  });

  it('atualiza com PUT no id', () => {
    service.atualizar(5, dto).subscribe();
    const req = http.expectOne(`${url}/5`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(dto);
    req.flush(null);
  });

  it('busca papéis administrativos', () => {
    service.papeis().subscribe();
    const req = http.expectOne(`${environment.apiUrl}/papeis-administrativos`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('busca módulos do sistema', () => {
    service.modulos().subscribe();
    const req = http.expectOne(`${environment.apiUrl}/modulos-sistema`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('busca permissões padrão do papel', () => {
    service.permissoesPadrao(3).subscribe();
    const req = http.expectOne(`${environment.apiUrl}/admin/papeis/3/permissoes-padrao`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
