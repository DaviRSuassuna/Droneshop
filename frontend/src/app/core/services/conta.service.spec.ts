import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { ContaService } from './conta.service';

describe('ContaService', () => {
  const api = environment.apiUrl;
  let service: ContaService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ContaService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('cartões: lista, salva, exclui e troca o padrão (#28)', () => {
    service.cartoes().subscribe();
    expect(http.expectOne(`${api}/conta/cartoes`).request.method).toBe('GET');
    const c = {
      tokenGateway: 'card_tok',
      ultimosDigitos: '4242',
      bandeira: 'visa',
      validade: '12/30',
    };
    service.salvarCartao(c).subscribe();
    const post = http.expectOne(`${api}/conta/cartoes`);
    expect(post.request.method).toBe('POST');
    expect(post.request.body).toEqual(c);
    service.excluirCartao(3).subscribe();
    expect(http.expectOne(`${api}/conta/cartoes/3`).request.method).toBe('DELETE');
    service.tornarPadrao(3).subscribe();
    expect(http.expectOne(`${api}/conta/cartoes/3/padrao`).request.method).toBe('PATCH');
  });

  it('drones: lista e registra a ANAC (#88)', () => {
    service.drones().subscribe();
    expect(http.expectOne(`${api}/conta/drones`).request.method).toBe('GET');
    service.registrarAnac(7, 'PP-1').subscribe();
    const req = http.expectOne(`${api}/conta/drones/7`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ registroAnac: 'PP-1' });
  });

  it('LGPD: termos, consentimentos e exclusão (#46)', () => {
    service.termos().subscribe();
    expect(http.expectOne(`${api}/loja/termos`).request.method).toBe('GET');
    service.consentimentos().subscribe();
    expect(http.expectOne(`${api}/conta/consentimentos`).request.method).toBe('GET');
    service.consentir('MARKETING', '2026-10').subscribe();
    const post = http.expectOne(`${api}/conta/consentimentos`);
    expect(post.request.body).toEqual({ tipo: 'MARKETING', versao: '2026-10' });
    service.excluirConta('segredo').subscribe();
    const ex = http.expectOne(`${api}/conta/exclusao`);
    expect(ex.request.method).toBe('POST');
    expect(ex.request.body).toEqual({ senha: 'segredo' });
  });

  it('2FA: gera, confirma e desliga (#45)', () => {
    service.gerar2fa().subscribe();
    expect(http.expectOne(`${api}/conta/2fa`).request.method).toBe('POST');
    service.confirmar2fa('123456').subscribe();
    expect(http.expectOne(`${api}/conta/2fa/confirmar`).request.body).toEqual({ codigo: '123456' });
    service.desligar2fa('123456').subscribe();
    const del = http.expectOne(`${api}/conta/2fa`);
    expect(del.request.method).toBe('DELETE');
    expect(del.request.body).toEqual({ codigo: '123456' });
  });

  it('favoritos: mover, compartilhar e lista pública (#86)', () => {
    service.moverFavoritosParaCarrinho().subscribe();
    expect(http.expectOne(`${api}/conta/desejos/mover-para-carrinho`).request.method).toBe('POST');
    service.compartilharFavoritos().subscribe();
    expect(http.expectOne(`${api}/conta/desejos/compartilhar`).request.method).toBe('POST');
    service.desligarCompartilhamento().subscribe();
    expect(http.expectOne(`${api}/conta/desejos/compartilhar`).request.method).toBe('DELETE');
    service.listaCompartilhada('abc').subscribe();
    expect(http.expectOne(`${api}/loja/listas/abc`).request.method).toBe('GET');
  });
});
