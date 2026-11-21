import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { LojaService } from './loja.service';

describe('LojaService', () => {
  const url = `${environment.apiUrl}/loja`;
  let service: LojaService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(LojaService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista as categorias do menu, com e sem tipo', () => {
    service.listarCategorias().subscribe();
    const todas = http.expectOne((r) => r.url === `${url}/categorias`);
    expect(todas.request.method).toBe('GET');
    expect(todas.request.params.has('tipo')).toBe(false);
    todas.flush([]);

    service.listarCategorias('DRONE').subscribe();
    const drones = http.expectOne((r) => r.url === `${url}/categorias`);
    expect(drones.request.params.get('tipo')).toBe('DRONE');
    drones.flush([]);
  });

  it('lista as marcas em destaque', () => {
    service.listarMarcas(true).subscribe();
    const req = http.expectOne((r) => r.url === `${url}/marcas`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('destaque')).toBe('true');
    req.flush([]);
  });

  it('busca pelo termo com paginação (#43)', () => {
    service.buscar(' fálcon ', 2, 12).subscribe();
    const req = http.expectOne((r) => r.url === `${url}/busca`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('q')).toBe('fálcon');
    expect(req.request.params.get('page')).toBe('2');
    expect(req.request.params.get('pageSize')).toBe('12');
    req.flush({ items: [], page: 2, pageSize: 12, totalItems: 0, totalPages: 0 });
  });

  it('lista produtos paginados sem mandar filtro vazio', () => {
    service
      .listarProdutos(
        { tipo: 'DRONE', marca: 'aerion', nome: ' ', precoMin: null, ordem: 'preco' },
        1,
        9,
      )
      .subscribe();
    const req = http.expectOne((r) => r.url === `${url}/produtos`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('pageSize')).toBe('9');
    expect(req.request.params.get('tipo')).toBe('DRONE');
    expect(req.request.params.get('marca')).toBe('aerion');
    expect(req.request.params.get('ordem')).toBe('preco');
    expect(req.request.params.has('nome')).toBe(false);
    expect(req.request.params.has('precoMin')).toBe(false);
    req.flush({ items: [], page: 1, pageSize: 9, totalItems: 0, totalPages: 0 });
  });

  it('busca as facetas da categoria', () => {
    service.buscarFiltros('profissionais').subscribe();
    const req = http.expectOne((r) => r.url === `${url}/filtros`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('categoria')).toBe('profissionais');
    req.flush({ marcas: [], preco: { min: null, max: null } });
  });

  it('busca o produto, os compatíveis, as ofertas e a configuração', () => {
    service.buscarProduto('falcon x2').subscribe();
    http.expectOne(`${url}/produtos/falcon%20x2`).flush({});
    service.listarCompativeis('falcon-x2').subscribe();
    http.expectOne(`${url}/produtos/falcon-x2/compativeis`).flush([]);
    service.listarOfertas().subscribe();
    const ofertas = http.expectOne(`${url}/ofertas`);
    expect(ofertas.request.method).toBe('GET');
    ofertas.flush([]);
    service.buscarConfiguracao().subscribe();
    http.expectOne(`${url}/configuracao`).flush({});
  });
});
