import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { ChamadoService } from './chamado.service';

describe('ChamadoService', () => {
  const api = environment.apiUrl;
  let service: ChamadoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(ChamadoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('abre o chamado com POST', () => {
    const dto = { assunto: 'Drone não liga', idPedido: 9, mensagem: 'Não liga' };
    service.abrir(dto).subscribe();
    const req = http.expectOne(`${api}/chamados`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
    req.flush({});
  });

  it('lista os chamados da conta', () => {
    service.meus().subscribe();
    const req = http.expectOne(`${api}/conta/chamados`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('lê e responde as mensagens como cliente', () => {
    service.mensagens(5).subscribe();
    const get = http.expectOne(`${api}/chamados/5/mensagens`);
    expect(get.request.method).toBe('GET');
    get.flush([]);
    service.responder(5, 'Sim').subscribe();
    const post = http.expectOne(`${api}/chamados/5/mensagens`);
    expect(post.request.method).toBe('POST');
    expect(post.request.body).toEqual({ texto: 'Sim' });
    post.flush({});
  });

  it('lista no admin com filtro de status e paginação', () => {
    service.listarAdmin(2, 1, 10).subscribe();
    const req = http.expectOne((r) => r.url === `${api}/admin/chamados`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('idStatus')).toBe('2');
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('pageSize')).toBe('10');
    req.flush({ items: [], page: 1, pageSize: 10, totalItems: 0, totalPages: 0 });
  });

  it('busca, atende e muda o status no admin', () => {
    service.buscarAdmin(7).subscribe();
    http.expectOne(`${api}/admin/chamados/7`).flush({});
    service.atender(7, 'Olá').subscribe();
    const msg = http.expectOne(`${api}/admin/chamados/7/mensagens`);
    expect(msg.request.method).toBe('POST');
    expect(msg.request.body).toEqual({ texto: 'Olá' });
    msg.flush({});
    service.alterarStatus(7, 3).subscribe();
    const st = http.expectOne(`${api}/admin/chamados/7/status`);
    expect(st.request.method).toBe('PATCH');
    expect(st.request.body).toEqual({ idStatus: 3 });
    st.flush(null);
  });
});
