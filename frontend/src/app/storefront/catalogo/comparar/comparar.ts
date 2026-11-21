import { Component, ElementRef, computed, effect, inject, input, signal, viewChild } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Comparacao, ComparacaoSpec } from '../../../core/models/comparacao.model';
import { ProdutoLojaItem } from '../../../core/models/loja.model';
import { LojaService } from '../../../core/services/loja.service';
import { PrecoPipe } from '../preco.pipe';

const MAXIMO = 4;

/**
 * Seção "Compare as versões" da página do produto (#44, protótipo produto.html#comparar).
 * Abre com as versões do grupo (D40) e aceita outros drones do catálogo, até 4.
 */
@Component({
  selector: 'app-comparar',
  imports: [RouterLink, PrecoPipe],
  templateUrl: './comparar.html',
})
export class Comparar {
  private readonly loja = inject(LojaService);

  readonly idProduto = input.required<number>();
  readonly nome = input.required<string>();
  readonly grupo = input<string | null>(null);

  readonly extras = signal<number[]>([]);
  readonly comparacao = signal<Comparacao | null>(null);
  readonly erro = signal<string | null>(null);
  readonly busca = signal('');
  readonly opcoes = signal<ProdutoLojaItem[]>([]);

  private readonly dialogo = viewChild<ElementRef<HTMLDialogElement>>('dlg');

  readonly naTabela = computed(
    () => this.comparacao()?.produtos.map((p) => p.id) ?? [this.idProduto()],
  );
  readonly cheia = computed(() => this.naTabela().length >= MAXIMO);

  constructor() {
    effect(() => {
      const grupo = this.grupo();
      const ids = grupo ? this.extras() : [this.idProduto(), ...this.extras()];
      if (!grupo && ids.length < 2) {
        this.comparacao.set(null);
        return;
      }
      this.loja.comparar(grupo, ids).subscribe({
        next: (c) => {
          this.comparacao.set(c);
          this.erro.set(null);
        },
        error: () => this.erro.set('Não foi possível montar a comparação.'),
      });
    });
  }

  valor(spec: ComparacaoSpec, i: number): string {
    const v = spec.valores[i];
    if (v === null || v === undefined || v === '') return '—';
    if (typeof v === 'boolean') return v ? '✓ sim' : 'não';
    return `${v}`;
  }

  temExtras(c: Comparacao): boolean {
    return c.produtos.some((p) => p.itensExtras.length > 0);
  }

  abrir(): void {
    this.procurar('');
    this.dialogo()?.nativeElement.showModal();
  }

  fechar(): void {
    this.dialogo()?.nativeElement.close();
  }

  procurar(q: string): void {
    this.busca.set(q);
    this.loja.listarProdutos({ tipo: 'DRONE', nome: q }, 0, 12).subscribe({
      next: (r) => this.opcoes.set(r.items),
      error: () => this.opcoes.set([]),
    });
  }

  adicionar(id: number): void {
    if (this.cheia() || this.naTabela().includes(id)) return;
    this.extras.update((l) => [...l, id]);
    this.fechar();
  }

  tirar(id: number): void {
    this.extras.update((l) => l.filter((x) => x !== id));
  }
}
