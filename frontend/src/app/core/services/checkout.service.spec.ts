import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { CarrinhoService, HEADER_CARRINHO } from './carrinho.service';
import { CheckoutService } from './checkout.service';

describe('CheckoutService', () => {
  let service: CheckoutService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: CarrinhoService, useValue: { token: () => 'tok-1' } },
      ],
    });
    service = TestBed.inject(CheckoutService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('cota o frete do carrinho com o CEP', () => {
    service.cotar('77001002').subscribe();
    const req = http.expectOne(`${environment.apiUrl}/frete/cotacao`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ cep: '77001002' });
    expect(req.request.headers.get(HEADER_CARRINHO)).toBe('tok-1');
    req.flush([]);
  });

  it('confirma o checkout com o corpo do contrato', () => {
    const dto = {
      idEndereco: 5,
      idModalidadeEntrega: 2,
      codigoServicoTransportadora: 'PAC',
      idMetodoPagamento: 1,
    };
    service.confirmar(dto).subscribe();
    const req = http.expectOne(`${environment.apiUrl}/checkout`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
    expect(req.request.headers.get(HEADER_CARRINHO)).toBe('tok-1');
    req.flush({});
  });
});
