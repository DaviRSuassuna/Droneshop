import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { PerguntaService } from './pergunta.service';

describe('PerguntaService', () => {
  const api = environment.apiUrl;
  let service: PerguntaService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(PerguntaService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista as aprovadas e pergunta pelo slug', () => {
    service.aprovadas('falcon-x2').subscribe();
    const get = http.expectOne(`${api}/loja/produtos/falcon-x2/perguntas`);
    expect(get.request.method).toBe('GET');
    get.flush([]);
    service.perguntar('falcon-x2', 'Funciona com óculos FPV?').subscribe();
    const post = http.expectOne(`${api}/loja/produtos/falcon-x2/perguntas`);
    expect(post.request.method).toBe('POST');
    expect(post.request.body).toEqual({ texto: 'Funciona com óculos FPV?' });
    post.flush({});
  });

  it('cliente responde pergunta aprovada', () => {
    service.responder(4, 'No meu deu 38').subscribe();
    const req = http.expectOne(`${api}/loja/perguntas/4/respostas`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ texto: 'No meu deu 38' });
    req.flush({});
  });

  it('modera e responde como loja no admin', () => {
    service.listarAdmin(1, 0, 20).subscribe();
    const lista = http.expectOne((r) => r.url === `${api}/admin/perguntas`);
    expect(lista.request.params.get('idStatus')).toBe('1');
    expect(lista.request.params.get('pageSize')).toBe('20');
    lista.flush({ items: [], page: 0, pageSize: 20, totalItems: 0, totalPages: 0 });
    service.moderar(4, 2).subscribe();
    const mod = http.expectOne(`${api}/admin/perguntas/4`);
    expect(mod.request.method).toBe('PATCH');
    expect(mod.request.body).toEqual({ idStatus: 2 });
    mod.flush(null);
    service.responderOficial(4, 'Cerca de 40 minutos').subscribe();
    const resp = http.expectOne(`${api}/admin/perguntas/4/respostas`);
    expect(resp.request.method).toBe('POST');
    expect(resp.request.body).toEqual({ texto: 'Cerca de 40 minutos' });
    resp.flush({});
  });
});
