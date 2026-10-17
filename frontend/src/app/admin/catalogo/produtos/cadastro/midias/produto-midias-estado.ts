import { HttpErrorResponse } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, concatMap, defer, from, last, map, of, tap } from 'rxjs';
import {
  MidiaProduto,
  NomeTipoMidia,
  TipoMidia,
} from '../../../../../core/models/midia-produto.model';
import { ProblemDetail } from '../../../../../core/models/problem.model';
import { MidiaProdutoService } from '../../../../../core/services/midia-produto.service';

/** Mídia na tela: já salva (tem id) ou na fila do produto novo (tem arquivo ou url). */
export interface ItemMidia {
  chave: string;
  id?: number;
  tipo: NomeTipoMidia;
  /** Endereço para exibir: arquivo da API, prévia local ou a URL do vídeo. */
  src: string;
  altText: string | null;
  principal: boolean;
  arquivo?: File;
  url?: string;
  nomeArquivo?: string;
}

export interface NovaMidia {
  tipo: NomeTipoMidia;
  arquivo?: File;
  url?: string;
  altText?: string | null;
  principal?: boolean;
}

/**
 * Mídias do cadastro de produto (#12). Produto salvo: cada ação vai direto para a API.
 * Produto novo: tudo fica numa fila local e sobe em enviarFila() depois que o produto é criado.
 * Fornecido pelo ProdutoCadastro, para o card do trilho e os campos de vídeo/manual dividirem o estado.
 */
@Injectable()
export class ProdutoMidiasEstado {
  private readonly service = inject(MidiaProdutoService);
  private sequencia = 0;

  readonly produtoId = signal<number | null>(null);
  readonly itens = signal<ItemMidia[]>([]);
  readonly ocupado = signal(false);
  readonly erro = signal<string | null>(null);
  private readonly tipos = signal<TipoMidia[]>([]);

  readonly imagens = computed(() => this.itens().filter((i) => i.tipo === 'IMAGEM'));
  readonly principal = computed(() => this.imagens().find((i) => i.principal) ?? null);
  readonly galeria = computed(() => this.imagens().filter((i) => !i.principal));
  readonly videos = computed(() => this.itens().filter((i) => i.tipo === 'VIDEO'));
  readonly manuais = computed(() => this.itens().filter((i) => i.tipo === 'MANUAL_PDF'));
  readonly temFila = computed(() => this.itens().some((i) => i.id === undefined));

  constructor() {
    this.service.listarTipos().subscribe({
      next: (tipos) => this.tipos.set(tipos),
      error: (err: HttpErrorResponse) =>
        this.falhou(err, 'Não foi possível carregar os tipos de mídia.'),
    });
  }

  carregar(idProduto: number): void {
    this.produtoId.set(idProduto);
    this.service.listar(idProduto).subscribe({
      next: (midias) => this.itens.set(midias.map((m) => this.daApi(m))),
      error: (err: HttpErrorResponse) => this.falhou(err, 'Não foi possível carregar as mídias.'),
    });
  }

  adicionar(nova: NovaMidia): void {
    this.erro.set(null);
    const idProduto = this.produtoId();
    if (idProduto === null) {
      this.itens.update((lista) => {
        const item = this.daFila(nova);
        const base = item.principal ? lista.map((i) => ({ ...i, principal: false })) : lista;
        return [...base, item];
      });
      return;
    }
    this.executar(this.enviar(idProduto, nova), 'Não foi possível enviar a mídia.', (midia) =>
      this.itens.update((lista) => [
        ...lista.map((i) => (midia.principal ? { ...i, principal: false } : i)),
        this.daApi(midia),
      ]),
    );
  }

  definirPrincipal(item: ItemMidia): void {
    this.erro.set(null);
    const marcar = () =>
      this.itens.update((lista) =>
        lista.map((i) => (i.tipo === 'IMAGEM' ? { ...i, principal: i.chave === item.chave } : i)),
      );
    if (item.id === undefined) return marcar();
    this.executar(
      this.service.alterar(item.id, { principal: true }),
      'Não foi possível definir a imagem principal.',
      marcar,
    );
  }

  /** Move a imagem dentro da galeria; a ordem é salva na hora. */
  mover(item: ItemMidia, direcao: -1 | 1): void {
    const galeria = this.galeria();
    const de = galeria.findIndex((i) => i.chave === item.chave);
    const para = de + direcao;
    if (de < 0 || para < 0 || para >= galeria.length) return;
    const novaGaleria = [...galeria];
    [novaGaleria[de], novaGaleria[para]] = [novaGaleria[para], novaGaleria[de]];

    const principal = this.principal();
    const anterior = this.itens();
    const novaLista = [
      ...(principal ? [principal] : []),
      ...novaGaleria,
      ...anterior.filter((i) => i.tipo !== 'IMAGEM'),
    ];
    this.itens.set(novaLista);

    const idProduto = this.produtoId();
    if (idProduto === null) return;
    this.erro.set(null);
    this.service
      .reordenar(
        idProduto,
        novaLista.map((i) => i.id as number),
      )
      .subscribe({
        error: (err: HttpErrorResponse) => {
          this.itens.set(anterior);
          this.falhou(err, 'Não foi possível salvar a ordem das imagens.');
        },
      });
  }

  excluir(item: ItemMidia): void {
    this.erro.set(null);
    const remover = () => {
      if (item.arquivo) URL.revokeObjectURL(item.src);
      this.itens.update((lista) => lista.filter((i) => i.chave !== item.chave));
    };
    if (item.id === undefined) return remover();
    this.executar(this.service.excluir(item.id), 'Não foi possível excluir a mídia.', remover);
  }

  /**
   * Sobe a fila do produto recém-criado, na ordem da tela. Para no primeiro erro:
   * o que já subiu fica salvo e o restante sai da fila (a tela passa a ser a de edição).
   */
  enviarFila(idProduto: number): Observable<void> {
    this.produtoId.set(idProduto);
    const fila = this.ordenadaParaEnvio().filter((i) => i.id === undefined);
    if (fila.length === 0) return of(undefined);
    this.ocupado.set(true);
    return from(fila).pipe(
      concatMap((item) =>
        this.enviar(idProduto, item).pipe(
          tap((midia) => {
            if (item.arquivo) URL.revokeObjectURL(item.src);
            this.itens.update((lista) =>
              lista.map((i) => (i.chave === item.chave ? this.daApi(midia) : i)),
            );
          }),
        ),
      ),
      last(),
      map(() => undefined),
      tap({
        complete: () => this.ocupado.set(false),
        error: (err: HttpErrorResponse) => {
          this.ocupado.set(false);
          this.falhou(err, 'Não foi possível enviar a mídia.');
          this.erro.update(
            (motivo) =>
              `O produto foi salvo, mas nem todas as mídias subiram (${motivo}). Adicione as que faltam.`,
          );
          for (const i of this.itens()) if (i.arquivo) URL.revokeObjectURL(i.src);
          this.itens.update((lista) => lista.filter((i) => i.id !== undefined));
        },
      }),
    );
  }

  /** Principal primeiro, depois a galeria e o resto, para a ordem na API bater com a tela. */
  private ordenadaParaEnvio(): ItemMidia[] {
    const principal = this.principal();
    return [
      ...(principal ? [principal] : []),
      ...this.galeria(),
      ...this.itens().filter((i) => i.tipo !== 'IMAGEM'),
    ];
  }

  private enviar(idProduto: number, nova: NovaMidia): Observable<MidiaProduto> {
    return defer(() => {
      const tipo = this.tipos().find((t) => t.nome === nova.tipo);
      if (!tipo) throw new Error('Tipos de mídia ainda não carregados.');
      return this.service.adicionar(idProduto, {
        idTipo: tipo.id,
        arquivo: nova.arquivo,
        url: nova.url,
        altText: nova.altText ?? undefined,
        principal: nova.principal ?? false,
      });
    });
  }

  private executar<T>(req$: Observable<T>, mensagem: string, aoTerminar: (r: T) => void): void {
    this.ocupado.set(true);
    req$.subscribe({
      next: (r) => {
        this.ocupado.set(false);
        aoTerminar(r);
      },
      error: (err: HttpErrorResponse) => {
        this.ocupado.set(false);
        this.falhou(err, mensagem);
      },
    });
  }

  private daApi(m: MidiaProduto): ItemMidia {
    return {
      chave: `id-${m.id}`,
      id: m.id,
      tipo: m.tipo.nome,
      src: m.tipo.nome === 'VIDEO' ? m.url : this.service.urlArquivo(m.url),
      altText: m.altText,
      principal: m.principal,
    };
  }

  private daFila(nova: NovaMidia): ItemMidia {
    return {
      chave: `fila-${++this.sequencia}`,
      tipo: nova.tipo,
      src: nova.arquivo ? URL.createObjectURL(nova.arquivo) : (nova.url ?? ''),
      altText: nova.altText ?? null,
      principal: nova.principal ?? false,
      arquivo: nova.arquivo,
      url: nova.url,
      nomeArquivo: nova.arquivo?.name,
    };
  }

  private falhou(err: unknown, padrao: string): void {
    const problem = err instanceof HttpErrorResponse ? (err.error as ProblemDetail | null) : null;
    this.erro.set(problem?.errors?.[0]?.message ?? problem?.detail ?? padrao);
  }
}
