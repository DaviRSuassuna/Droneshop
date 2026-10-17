import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { MidiaProdutoService } from './midia-produto.service';

describe('MidiaProdutoService', () => {
  const baseUrl = environment.apiUrl;
  let service: MidiaProdutoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(MidiaProdutoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista tipos e mídias do produto', () => {
    service.listarTipos().subscribe();
    const tipos = http.expectOne(`${baseUrl}/tipos-midia`);
    expect(tipos.request.method).toBe('GET');
    tipos.flush([]);

    service.listar(12).subscribe();
    const midias = http.expectOne(`${baseUrl}/produtos/12/midias`);
    expect(midias.request.method).toBe('GET');
    midias.flush([]);
  });

  it('envia upload multipart para o produto', () => {
    const arquivo = new File(['imagem'], 'drone.png', { type: 'image/png' });
    service
      .adicionar(12, {
        idTipo: 1,
        arquivo,
        altText: 'Drone de frente',
        principal: true,
        ordem: 2,
      })
      .subscribe();

    const req = http.expectOne(`${baseUrl}/produtos/12/midias`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toBeInstanceOf(FormData);
    expect(req.request.body.get('idTipo')).toBe('1');
    expect(req.request.body.get('arquivo')).toBe(arquivo);
    expect(req.request.body.get('altText')).toBe('Drone de frente');
    expect(req.request.body.get('principal')).toBe('true');
    expect(req.request.body.get('ordem')).toBe('2');
    req.flush({ id: 3 });
  });

  it('altera a mídia, reordena e exclui', () => {
    service.alterar(3, { principal: true }).subscribe();
    const alterar = http.expectOne(`${baseUrl}/midias/3`);
    expect(alterar.request.method).toBe('PATCH');
    expect(alterar.request.body).toEqual({ principal: true });
    alterar.flush(null);

    service.reordenar(12, [3, 4]).subscribe();
    const reordenar = http.expectOne(`${baseUrl}/produtos/12/midias/ordem`);
    expect(reordenar.request.method).toBe('PATCH');
    expect(reordenar.request.body).toEqual({ ids: [3, 4] });
    reordenar.flush(null);

    service.excluir(3).subscribe();
    const excluir = http.expectOne(`${baseUrl}/midias/3`);
    expect(excluir.request.method).toBe('DELETE');
    excluir.flush(null);
  });

  it('resolve a URL pública do arquivo com a base da API', () => {
    expect(service.urlArquivo('/midias/arquivos/arquivo.png')).toBe(
      `${baseUrl}/midias/arquivos/arquivo.png`,
    );
  });
});
