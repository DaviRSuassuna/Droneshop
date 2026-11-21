import { Component, computed, effect, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ProdutoLojaItem } from '../../../core/models/loja.model';
import { LojaService } from '../../../core/services/loja.service';
import { Pcard } from '../pcard/pcard';

const POR_PAGINA = 12;

/** Resultado da barra de busca (#43): nome, SKU, marca e tags, sem acento (D23). */
@Component({
  selector: 'app-busca',
  imports: [RouterLink, Pcard],
  templateUrl: './busca.html',
})
export class BuscaPage {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly loja = inject(LojaService);

  private readonly query = toSignal(this.rota.queryParamMap, {
    initialValue: this.rota.snapshot.queryParamMap,
  });
  readonly termo = computed(() => (this.query().get('q') ?? '').trim());
  readonly pagina = computed(() => Number(this.query().get('page') ?? 0));

  readonly produtos = signal<ProdutoLojaItem[]>([]);
  readonly total = signal(0);
  readonly totalPaginas = signal(0);
  readonly carregando = signal(false);
  readonly paginas = computed(() => Array.from({ length: this.totalPaginas() }, (_, i) => i));

  constructor() {
    effect(() => {
      const q = this.termo();
      const page = this.pagina();
      if (q.length < 2) {
        this.produtos.set([]);
        this.total.set(0);
        this.totalPaginas.set(0);
        return;
      }
      this.carregando.set(true);
      this.loja.buscar(q, page, POR_PAGINA).subscribe({
        next: (r) => {
          this.produtos.set(r.items);
          this.total.set(r.totalItems);
          this.totalPaginas.set(r.totalPages);
          this.carregando.set(false);
        },
        error: () => {
          this.produtos.set([]);
          this.total.set(0);
          this.carregando.set(false);
        },
      });
    });
  }

  irPara(page: number): void {
    this.router.navigate([], { queryParams: { page }, queryParamsHandling: 'merge' });
  }
}
