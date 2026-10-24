import {
  Component,
  ElementRef,
  computed,
  inject,
  input,
  model,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { catchError, debounceTime, distinctUntilChanged, map, of, switchMap } from 'rxjs';
import { ProdutoService } from '../../../../../core/services/produto.service';
import { ProdutoResumo, TIPO_PRODUTO } from '../../../../../core/models/produto.model';

/** Produto na lista "Compatível com"; salvo diz se o vínculo já está gravado na API. */
export interface VinculoCompativel {
  produto: ProdutoResumo;
  salvo: boolean;
}

export function rotuloTipo(produto: ProdutoResumo): string {
  if (produto.tipo.id === TIPO_PRODUTO.DRONE) return 'Drone';
  return produto.tipo.id === TIPO_PRODUTO.KIT ? 'Kit' : 'Acessório';
}

/**
 * Seção "Compatível com" do cadastro de acessório (D17): busca produtos por nome ou SKU
 * e monta a lista que vai em idsCompativeis.
 */
@Component({
  selector: 'app-compativeis-acessorio',
  imports: [ReactiveFormsModule],
  templateUrl: './compativeis-acessorio.html',
})
export class CompativeisAcessorio {
  private readonly produtoService = inject(ProdutoService);

  /** Acessório em edição: ele não aparece na busca, porque não é compatível consigo mesmo. */
  readonly idAtual = input<number | null>(null);
  readonly erro = input<string | null>(null);
  readonly vinculos = model<VinculoCompativel[]>([]);

  readonly busca = new FormControl('', { nonNullable: true });
  readonly buscou = signal(false);
  /** A busca aparece ao clicar em "Vincular produto", como no protótipo. */
  readonly buscando = signal(false);
  private readonly resultados = signal<ProdutoResumo[]>([]);
  private readonly campoBusca = viewChild<ElementRef<HTMLInputElement>>('campoBusca');
  readonly rotuloTipo = rotuloTipo;

  /** Resultado da busca sem o próprio acessório e sem o que já está na lista. */
  readonly opcoes = computed(() => {
    const vinculados = new Set(this.vinculos().map((v) => v.produto.id));
    return this.resultados().filter((p) => p.id !== this.idAtual() && !vinculados.has(p.id));
  });

  constructor() {
    this.busca.valueChanges
      .pipe(
        debounceTime(250),
        map((termo) => termo.trim()),
        distinctUntilChanged(),
        switchMap((termo) =>
          termo.length < 2
            ? of(null)
            : this.produtoService.listar({ busca: termo }, 0, 8).pipe(
                map((pagina) =>
                  pagina.items.map((p) => ({ id: p.id, nome: p.nome, tipo: p.tipo })),
                ),
                catchError(() => of([])),
              ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((lista) => {
        this.resultados.set(lista ?? []);
        this.buscou.set(lista !== null);
      });
  }

  vincular(produto: ProdutoResumo): void {
    this.vinculos.update((lista) => [...lista, { produto, salvo: false }]);
    // fecha a lista na hora, sem esperar o debounce da busca vazia
    this.resultados.set([]);
    this.buscou.set(false);
    this.busca.setValue('');
    this.focarBusca();
  }

  desvincular(id: number): void {
    this.vinculos.update((lista) => lista.filter((v) => v.produto.id !== id));
  }

  abrirBusca(): void {
    this.buscando.set(true);
    // o campo só existe depois que a busca abre
    setTimeout(() => this.campoBusca()?.nativeElement.focus());
  }

  private focarBusca(): void {
    this.campoBusca()?.nativeElement.focus();
  }
}
