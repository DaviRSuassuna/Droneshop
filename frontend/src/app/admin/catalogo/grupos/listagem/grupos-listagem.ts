import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { GrupoProdutoService } from '../../../../core/services/grupo-produto.service';
import { ProdutoService } from '../../../../core/services/produto.service';
import { GrupoProduto } from '../../../../core/models/grupo-produto.model';

/** Listagem de grupos de produto (D40), com filtro e paginação no servidor. */
@Component({
  selector: 'app-grupos-listagem',
  imports: [RouterLink],
  templateUrl: './grupos-listagem.html',
})
export class GruposListagem implements OnInit, OnDestroy {
  private readonly grupoService = inject(GrupoProdutoService);
  private readonly produtoService = inject(ProdutoService);
  private readonly router = inject(Router);

  readonly tamanhoPagina = 10;
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly grupos = signal<GrupoProduto[]>([]);
  readonly total = signal(0);
  readonly totalProdutos = signal(0);

  readonly termoBusca = signal('');
  readonly filtroAtivo = signal<boolean | null>(null);
  readonly pagina = signal(0);
  private timerBusca?: ReturnType<typeof setTimeout>;

  private readonly formatoMoeda = new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
    maximumFractionDigits: 0,
  });

  readonly totalPaginas = computed(() => Math.max(1, Math.ceil(this.total() / this.tamanhoPagina)));
  readonly produtosAgrupados = computed(() => this.grupos().reduce((t, g) => t + g.versoes.length, 0));
  readonly exibindo = computed(() => {
    if (this.total() === 0) return 'Exibindo 0 de 0';
    const inicio = this.pagina() * this.tamanhoPagina + 1;
    return `Exibindo ${inicio} a ${inicio + this.grupos().length - 1} de ${this.total()}`;
  });

  ngOnInit(): void {
    this.produtoService.listar({}, 0, 1).subscribe((p) => this.totalProdutos.set(p.totalItems));
    this.carregar();
  }

  ngOnDestroy(): void {
    clearTimeout(this.timerBusca);
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.grupoService
      .listar({ nome: this.termoBusca(), ativo: this.filtroAtivo() }, this.pagina(), this.tamanhoPagina)
      .subscribe({
        next: (p) => {
          this.grupos.set(p.items);
          this.total.set(p.totalItems);
          this.carregando.set(false);
        },
        error: () => {
          this.erro.set('Não foi possível carregar os grupos.');
          this.carregando.set(false);
        },
      });
  }

  aoDigitarBusca(valor: string): void {
    this.termoBusca.set(valor);
    clearTimeout(this.timerBusca);
    this.timerBusca = setTimeout(() => {
      this.pagina.set(0);
      this.carregar();
    }, 300);
  }

  aoMudarStatus(valor: string): void {
    this.filtroAtivo.set(valor === '' ? null : valor === 'true');
    this.pagina.set(0);
    this.carregar();
  }

  irParaPagina(p: number): void {
    if (p < 0 || p >= this.totalPaginas() || p === this.pagina()) return;
    this.pagina.set(p);
    this.carregar();
  }

  abrir(grupo: GrupoProduto): void {
    this.router.navigate(['/admin/grupos', grupo.id, 'editar']);
  }

  preco(valor: number | null): string {
    return valor === null ? '—' : this.formatoMoeda.format(valor);
  }
}
