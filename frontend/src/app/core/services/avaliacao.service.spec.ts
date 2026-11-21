import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { AvaliacaoService } from './avaliacao.service';

describe('AvaliacaoService', () => {
  const api = environment.apiUrl;
  let service: AvaliacaoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(AvaliacaoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista e envia avaliações pelo slug', () => {
    service.doProduto('falcon-x2').subscribe();
    const get = http.expectOne(`${api}/loja/produtos/falcon-x2/avaliacoes`);
    expect(get.request.method).toBe('GET');
    get.flush({ media: null, quantidade: 0, itens: [] });
    service.avaliar('falcon-x2', { nota: 5, comentario: 'Muito bom' }).subscribe();
    const post = http.expectOne(`${api}/loja/produtos/falcon-x2/avaliacoes`);
    expect(post.request.method).toBe('POST');
    expect(post.request.body).toEqual({ nota: 5, comentario: 'Muito bom' });
    post.flush({});
  });

  it('lista e modera no admin', () => {
    service.listarAdmin(1, 2, 10).subscribe();
    const lista = http.expectOne((r) => r.url === `${api}/admin/avaliacoes`);
    expect(lista.request.method).toBe('GET');
    expect(lista.request.params.get('idStatus')).toBe('1');
    expect(lista.request.params.get('page')).toBe('2');
    expect(lista.request.params.get('pageSize')).toBe('10');
    lista.flush({ items: [], page: 2, pageSize: 10, totalItems: 0, totalPages: 0 });
    service.moderar(9, 2).subscribe();
    const mod = http.expectOne(`${api}/admin/avaliacoes/9`);
    expect(mod.request.method).toBe('PATCH');
    expect(mod.request.body).toEqual({ idStatus: 2 });
    mod.flush(null);
  });
});
