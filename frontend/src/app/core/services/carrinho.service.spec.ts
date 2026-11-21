import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { Carrinho } from '../models/compra.model';
import { CHAVE_CARRINHO, CarrinhoService, HEADER_CARRINHO } from './carrinho.service';

function armazenamento(): Storage {
  const dados = new Map<string, string>();
  return {
    get length() {
      return dados.size;
    },
    clear: () => dados.clear(),
    getItem: (k) => dados.get(k) ?? null,
    key: (i) => Array.from(dados.keys())[i] ?? null,
    removeItem: (k) => void dados.delete(k),
    setItem: (k, v) => void dados.set(String(k), String(v)),
  };
}

const carrinho: Carrinho = {
  token: 'tok-1',
  status: { id: 1, nome: 'ATIVO' },
  itens: [
    {
      id: 5,
      produto: { id: 31, sku: 'A', nome: 'A', slug: 'a' },
      quantidade: 2,
      precoUnitario: 10,
      subtotal: 20,
      disponivel: true,
      reservas: [],
    },
  ],
  subtotal: 20,
  desconto: 0,
  total: 20,
  cupom: null,
  avisos: [],
};

describe('CarrinhoService', () => {
  const url = `${environment.apiUrl}/carrinho`;
  let service: CarrinhoService;
  let http: HttpTestingController;

  beforeEach(() => {
    Object.defineProperty(globalThis, 'localStorage', {
      configurable: true,
      value: armazenamento(),
    });
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CarrinhoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('cria o carrinho, guarda o token e conta os itens', () => {
    service.criar().subscribe();
    const req = http.expectOne(url);
    expect(req.request.method).toBe('POST');
    req.flush(carrinho);
    expect(localStorage.getItem(CHAVE_CARRINHO)).toBe('tok-1');
    expect(service.quantidade()).toBe(2);
  });

  it('manda o token no header e o corpo do item', () => {
    localStorage.setItem(CHAVE_CARRINHO, 'tok-1');
    service.adicionar(31, 2).subscribe();
    const req = http.expectOne(`${url}/itens`);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get(HEADER_CARRINHO)).toBe('tok-1');
    expect(req.request.body).toEqual({ idProduto: 31, quantidade: 2 });
    req.flush(carrinho);
  });

  it('altera, remove, reserva e mexe no cupom', () => {
    localStorage.setItem(CHAVE_CARRINHO, 'tok-1');
    service.alterar(5, 3).subscribe();
    const patch = http.expectOne(`${url}/itens/5`);
    expect(patch.request.method).toBe('PATCH');
    expect(patch.request.body).toEqual({ quantidade: 3 });
    patch.flush(carrinho);

    service.remover(5).subscribe();
    expect(http.expectOne(`${url}/itens/5`).request.method).toBe('DELETE');

    service.reservar().subscribe();
    expect(http.expectOne(`${url}/reservar`).request.method).toBe('POST');

    service.aplicarCupom('BEMVINDO10').subscribe();
    const cupom = http.expectOne(`${url}/cupom`);
    expect(cupom.request.method).toBe('POST');
    expect(cupom.request.body).toEqual({ codigo: 'BEMVINDO10' });

    service.removerCupom().subscribe();
    const sem = http.match(`${url}/cupom`);
    expect(sem[0].request.method).toBe('DELETE');
  });

  it('busca o carrinho do cliente e vincula o anônimo', () => {
    service.doCliente().subscribe();
    expect(http.expectOne(`${environment.apiUrl}/conta/carrinho`).request.method).toBe('GET');
    localStorage.setItem(CHAVE_CARRINHO, 'tok-1');
    service.vincular().subscribe();
    const req = http.expectOne(`${url}/vincular`);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get(HEADER_CARRINHO)).toBe('tok-1');
  });
});
