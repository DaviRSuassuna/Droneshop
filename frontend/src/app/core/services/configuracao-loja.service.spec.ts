import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { ConfiguracaoLojaDTO } from '../models/configuracao-loja.model';
import { ConfiguracaoLojaService } from './configuracao-loja.service';

describe('ConfiguracaoLojaService', () => {
  const url = `${environment.apiUrl}/admin/configuracao`;
  const dto: ConfiguracaoLojaDTO = {
    dadosFiscais: 'Aeria Drones Comércio Ltda.',
    cepOrigemFrete: '77001000',
    remetentePadrao: 'Aeria Drones',
    prazoArrependimentoDias: 7,
    prazoGarantiaDias: 365,
    prazoAnaliseDevolucaoDiasUteis: 5,
    freteReversoPorContaLoja: true,
  };
  let service: ConfiguracaoLojaService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ConfiguracaoLojaService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('busca a configuração única da loja', () => {
    service.buscar().subscribe((configuracao) => expect(configuracao.id).toBe(1));
    const req = http.expectOne(url);
    expect(req.request.method).toBe('GET');
    req.flush({ id: 1, ...dto });
  });

  it('atualiza a configuração com PUT e sem id no corpo', () => {
    service.atualizar(dto).subscribe();
    const req = http.expectOne(url);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(dto);
    req.flush(null);
  });
});
