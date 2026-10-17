import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../../../../environments/environment';
import { ProdutoMidiasEstado } from './produto-midias-estado';

describe('ProdutoMidiasEstado', () => {
  const baseUrl = environment.apiUrl;
  let estado: ProdutoMidiasEstado;
  let http: HttpTestingController;

  const png = (nome: string) => new File(['x'], nome, { type: 'image/png' });
  const midia = (id: number, nome: 'IMAGEM' | 'VIDEO', principal: boolean) => ({
    id,
    tipo: { id: nome === 'IMAGEM' ? 1 : 2, nome },
    url: `/midias/arquivos/${id}.png`,
    principal,
    ordem: id,
    altText: 'x',
  });

  beforeEach(() => {
    URL.createObjectURL = () => 'blob:previa';
    URL.revokeObjectURL = () => undefined;
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), ProdutoMidiasEstado],
    });
    estado = TestBed.inject(ProdutoMidiasEstado);
    http = TestBed.inject(HttpTestingController);
    http.expectOne(`${baseUrl}/tipos-midia`).flush([
      { id: 1, nome: 'IMAGEM' },
      { id: 2, nome: 'VIDEO' },
      { id: 3, nome: 'MANUAL_PDF' },
    ]);
  });

  afterEach(() => http.verify());

  it('produto novo guarda as mídias na fila sem chamar a API', () => {
    estado.adicionar({ tipo: 'IMAGEM', arquivo: png('a.png'), altText: 'A', principal: true });
    estado.adicionar({ tipo: 'IMAGEM', arquivo: png('b.png'), altText: 'B', principal: true });
    expect(estado.temFila()).toBe(true);
    expect(estado.principal()?.altText).toBe('B');
    expect(estado.galeria().map((i) => i.altText)).toEqual(['A']);
  });

  it('enviarFila sobe a principal primeiro e depois a galeria', () => {
    estado.adicionar({ tipo: 'IMAGEM', arquivo: png('g.png'), altText: 'Galeria' });
    estado.adicionar({
      tipo: 'IMAGEM',
      arquivo: png('p.png'),
      altText: 'Principal',
      principal: true,
    });
    let concluiu = false;
    estado.enviarFila(9).subscribe({ complete: () => (concluiu = true) });

    const primeira = http.expectOne(`${baseUrl}/produtos/9/midias`);
    expect((primeira.request.body as FormData).get('altText')).toBe('Principal');
    primeira.flush(midia(1, 'IMAGEM', true));
    const segunda = http.expectOne(`${baseUrl}/produtos/9/midias`);
    expect((segunda.request.body as FormData).get('altText')).toBe('Galeria');
    segunda.flush(midia(2, 'IMAGEM', false));

    expect(concluiu).toBe(true);
    expect(estado.temFila()).toBe(false);
  });

  it('erro na fila tira o restante e avisa que o produto foi salvo', () => {
    estado.adicionar({ tipo: 'IMAGEM', arquivo: png('a.png'), altText: 'A', principal: true });
    estado.adicionar({ tipo: 'VIDEO', url: 'https://youtu.be/x' });
    estado.enviarFila(9).subscribe({ error: () => undefined });
    http.expectOne(`${baseUrl}/produtos/9/midias`).flush(midia(1, 'IMAGEM', true));
    http
      .expectOne(`${baseUrl}/produtos/9/midias`)
      .flush({ detail: 'URL inválida.' }, { status: 400, statusText: 'Bad Request' });
    expect(estado.temFila()).toBe(false);
    expect(estado.itens().length).toBe(1);
    expect(estado.erro()).toContain('O produto foi salvo');
  });

  it('produto salvo reordena a galeria na hora', () => {
    estado.carregar(9);
    http
      .expectOne(`${baseUrl}/produtos/9/midias`)
      .flush([midia(1, 'IMAGEM', true), midia(2, 'IMAGEM', false), midia(3, 'IMAGEM', false)]);
    estado.mover(estado.galeria()[1], -1);
    const req = http.expectOne(`${baseUrl}/produtos/9/midias/ordem`);
    expect(req.request.body).toEqual({ ids: [1, 3, 2] });
    req.flush(null);
  });
});
