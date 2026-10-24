import { TestBed } from '@angular/core/testing';
import { of, Subject } from 'rxjs';
import { Cupom, UsoCupom } from '../../../core/models/cupom.model';
import { PageResponse } from '../../../core/models/page.model';
import { CupomService } from '../../../core/services/cupom.service';
import { CuponsListagem } from './cupons-listagem';

const cupom: Cupom = {
  id: 42,
  codigo: 'ESCOLHIDO',
  tipoDesconto: { id: 1, nome: 'PERCENTUAL' },
  valor: 10,
  dataInicio: '2026-01-01T00:00:00',
  dataFim: null,
  valorMinimo: null,
  limiteTotalUso: null,
  limitePorCliente: null,
  ativo: true,
  situacao: 'VIGENTE',
  usos: 2,
  categorias: [],
  marcas: [],
  produtos: [],
};
const pagina = (page: number, items: UsoCupom[], totalPages = 2): PageResponse<UsoCupom> => ({
  items,
  page,
  pageSize: 1000,
  totalPages,
  totalItems: 2,
});
const uso = (id: number, nome: string): UsoCupom => ({
  id,
  pedido: { id },
  cliente: { id, nome },
  dataUso: '2026-10-24T12:00:00',
});

function lerBlob(blob: Blob): Promise<string> {
  return new Promise((resolve, reject) => {
    const leitor = new FileReader();
    leitor.onload = () => resolve(String(leitor.result));
    leitor.onerror = () => reject(leitor.error);
    leitor.readAsText(blob);
  });
}

describe('Exportação de usos do cupom selecionado', () => {
  let tela: CuponsListagem;
  const usos = vi.fn();
  const criarUrl = vi.fn();

  beforeEach(() => {
    usos.mockReset();
    criarUrl.mockReset().mockReturnValue('blob:teste');
    TestBed.configureTestingModule({ providers: [{ provide: CupomService, useValue: { usos } }] });
    tela = TestBed.runInInjectionContext(() => new CuponsListagem());
    vi.stubGlobal('URL', { createObjectURL: criarUrl, revokeObjectURL: vi.fn() });
    vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => undefined);
  });
  afterEach(() => {
    vi.restoreAllMocks();
    vi.unstubAllGlobals();
  });

  it('usa o cupom escolhido, lê todas as páginas e baixa um único CSV completo', async () => {
    const primeira = new Subject<PageResponse<UsoCupom>>();
    const segunda = new Subject<PageResponse<UsoCupom>>();
    usos.mockReturnValueOnce(primeira).mockReturnValueOnce(segunda);
    tela.cupons.set([{ ...cupom, id: 1, codigo: 'PRIMEIRO' }, cupom]);
    tela.exportarUsos(cupom);
    primeira.next(pagina(0, [uso(11, 'Cliente "A"')]));
    primeira.complete();
    expect(criarUrl).not.toHaveBeenCalled();
    segunda.next(pagina(1, [uso(12, 'Cliente B')]));
    segunda.complete();
    expect(usos.mock.calls).toEqual([[42], [42, 1]]);
    expect(criarUrl).toHaveBeenCalledTimes(1);
    expect(HTMLAnchorElement.prototype.click).toHaveBeenCalledTimes(1);
    const link = vi.mocked(HTMLAnchorElement.prototype.click).mock.instances[0];
    expect(link).toBeInstanceOf(HTMLAnchorElement);
    if (!(link instanceof HTMLAnchorElement)) throw new Error('O download deve usar um link HTML.');
    expect(link.download).toBe('usos-escolhido.csv');
    const csv = await lerBlob(criarUrl.mock.calls[0][0] as Blob);
    expect(csv).toContain('"ESCOLHIDO","11","Cliente ""A"""');
    expect(csv).toContain('"ESCOLHIDO","12","Cliente B"');
    expect(csv).not.toContain('PRIMEIRO');
    expect(tela.exportando()).toBeNull();
  });

  it('não baixa CSV parcial se uma página falha e permite tentar novamente', () => {
    const primeira = new Subject<PageResponse<UsoCupom>>();
    const segunda = new Subject<PageResponse<UsoCupom>>();
    usos.mockReturnValueOnce(primeira).mockReturnValueOnce(segunda);
    tela.exportarUsos(cupom);
    primeira.next(pagina(0, [uso(11, 'Cliente A')]));
    primeira.complete();
    segunda.error({ error: { detail: 'Falha ao consultar os usos.' } });
    expect(criarUrl).not.toHaveBeenCalled();
    expect(tela.erroExportacao()).toBe('Falha ao consultar os usos.');
    expect(tela.exportando()).toBeNull();
    usos.mockReturnValueOnce(of(pagina(0, [], 0)));
    tela.exportarUsos(cupom);
    expect(criarUrl).toHaveBeenCalledTimes(1);
    expect(tela.erroExportacao()).toBeNull();
  });

  it('evita exportações simultâneas enquanto a consulta está em andamento', () => {
    const resposta = new Subject<PageResponse<UsoCupom>>();
    usos.mockReturnValue(resposta);
    tela.exportarUsos(cupom);
    tela.exportarUsos({ ...cupom, id: 99 });
    expect(usos).toHaveBeenCalledTimes(1);
    expect(tela.exportando()).toBe(42);
    resposta.next(pagina(0, [], 1));
    resposta.complete();
    expect(tela.exportando()).toBeNull();
  });
});
