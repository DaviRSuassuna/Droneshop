import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { PreferenciaNotificacaoService } from './preferencia-notificacao.service';

describe('PreferenciaNotificacaoService', () => {
  const api = environment.apiUrl;
  let service: PreferenciaNotificacaoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(PreferenciaNotificacaoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lê as preferências da conta', () => {
    service.listar().subscribe();
    const req = http.expectOne(`${api}/conta/preferencias-notificacao`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('salva as preferências com PUT da lista', () => {
    const lista = [{ tipoNotificacao: 'MARKETING', habilitado: true }];
    service.salvar(lista).subscribe();
    const req = http.expectOne(`${api}/conta/preferencias-notificacao`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(lista);
    req.flush(null);
  });

  it('descadastra pelo token, sem login', () => {
    service.descadastrar('abc_123').subscribe();
    const req = http.expectOne(`${api}/descadastro/abc_123`);
    expect(req.request.method).toBe('GET');
    req.flush({ tipoNotificacao: 'CARRINHO_ABANDONADO', habilitado: false });
  });
});
