import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { NotificacaoService } from './notificacao.service';

describe('NotificacaoService', () => {
  const url = `${environment.apiUrl}/admin`;
  let service: NotificacaoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(NotificacaoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista os templates', () => {
    service.templates().subscribe();
    const req = http.expectOne(`${url}/templates-email`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('busca um template pelo código', () => {
    service.template('PEDIDO_ENVIADO').subscribe();
    const req = http.expectOne(`${url}/templates-email/PEDIDO_ENVIADO`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('edita o template com PUT', () => {
    const dto = { assunto: 'Saiu', corpoTemplate: 'Olá {{nome}}' };
    service.editar('PEDIDO_ENVIADO', dto).subscribe();
    const req = http.expectOne(`${url}/templates-email/PEDIDO_ENVIADO`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(dto);
    req.flush({});
  });

  it('lista notificações com filtros e paginação', () => {
    service.notificacoes({ destinatario: ' a@b.com ', idStatusEnvio: 6, page: 1, pageSize: 10 }).subscribe();
    const req = http.expectOne((r) => r.url === `${url}/notificacoes`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('destinatario')).toBe('a@b.com');
    expect(req.request.params.get('idStatusEnvio')).toBe('6');
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('pageSize')).toBe('10');
    req.flush({ items: [], page: 1, pageSize: 10, totalItems: 0, totalPages: 0 });
  });

  it('ignora filtros vazios', () => {
    service.notificacoes({ destinatario: ' ' }).subscribe();
    const req = http.expectOne((r) => r.url === `${url}/notificacoes`);
    expect(req.request.params.has('destinatario')).toBe(false);
    expect(req.request.params.has('idStatusEnvio')).toBe(false);
    req.flush({ items: [], page: 0, pageSize: 20, totalItems: 0, totalPages: 0 });
  });
});
