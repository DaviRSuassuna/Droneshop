import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AdminNav } from '../../../../layout/admin/admin-nav/admin-nav';
import { ProdutoService } from '../../../../core/services/produto.service';
import { MarcaService } from '../../../../core/services/marca.service';
import { CategoriaService } from '../../../../core/services/categoria.service';
import { SubcategoriaService } from '../../../../core/services/subcategoria.service';
import { FiltroProduto, Produto, StatusProduto } from '../../../../core/models/produto.model';
import { Marca } from '../../../../core/models/marca.model';
import { Categoria } from '../../../../core/models/categoria.model';
import { Subcategoria } from '../../../../core/models/subcategoria.model';

/**
 * Listagem administrativa de Produto (Produto N:1 Marca e Produto N:1 Subcategoria N:1 Categoria).
 * Diferente das outras listagens, aqui filtro e paginação rodam no servidor:
 * cada mudança dispara GET /produtos (página) + GET /produtos/count (total).
 */
@Component({
  selector: 'app-produtos-listagem',
  standalone: true,
  imports: [RouterLink, AdminNav],
  templateUrl: './produtos-listagem.html',
})
export class ProdutosListagem implements OnInit, OnDestroy {
  private readonly produtoService = inject(ProdutoService);
  private readonly marcaService = inject(MarcaService);
  private readonly categoriaService = inject(CategoriaService);
  private readonly subcategoriaService = inject(SubcategoriaService);

  readonly tamanhosPagina = [5, 10, 20];
  readonly grid = 'minmax(0, 1fr) 130px 200px 120px 100px 64px';

  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly produtos = signal<Produto[]>([]);
  readonly total = signal(0);

  // opções dos selects (lado "one" das relações)
  readonly marcas = signal<Marca[]>([]);
  readonly categorias = signal<Categoria[]>([]);
  readonly subcategorias = signal<Subcategoria[]>([]);

  // filtros
  readonly termoBusca = signal('');
  readonly filtroMarca = signal<number | null>(null);
  readonly filtroCategoria = signal<number | null>(null);
  readonly filtroSubcategoria = signal<number | null>(null);
  readonly filtroStatus = signal<StatusProduto | ''>('');

  // paginação
  readonly pagina = signal(0);
  readonly tamanhoPagina = signal(10);

  private readonly formatoMoeda = new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  });

  /** O select de subcategoria só mostra as da categoria escolhida (ou todas, se nenhuma). */
  readonly subcategoriasDoFiltro = computed(() => {
    const categoria = this.filtroCategoria();
    return categoria
      ? this.subcategorias().filter((s) => s.categoriaId === categoria)
      : this.subcategorias();
  });

  private timerBusca?: ReturnType<typeof setTimeout>;

  readonly totalPaginas = computed(() =>
    Math.max(1, Math.ceil(this.total() / this.tamanhoPagina())),
  );

  readonly intervaloExibido = computed(() => {
    if (this.total() === 0) return '0';
    const inicio = this.pagina() * this.tamanhoPagina() + 1;
    const fim = Math.min(inicio + this.produtos().length - 1, this.total());
    return `${inicio}-${fim}`;
  });

  /** Números de página a exibir, com null representando reticências. Ex: 1 … 4 5 6 … 12 */
  readonly paginasVisiveis = computed<(number | null)[]>(() => {
    const total = this.totalPaginas();
    const atual = this.pagina();
    if (total <= 7) return Array.from({ length: total }, (_, i) => i);
    const set = new Set([0, total - 1, atual - 1, atual, atual + 1]);
    const ordenadas = [...set].filter((p) => p >= 0 && p < total).sort((a, b) => a - b);
    const resultado: (number | null)[] = [];
    ordenadas.forEach((p, i) => {
      if (i > 0 && p - ordenadas[i - 1] > 1) resultado.push(null);
      resultado.push(p);
    });
    return resultado;
  });

  readonly filtrosAtivos = computed(() => {
    const chips: { rotulo: string; limpar: () => void }[] = [];
    if (this.termoBusca().trim())
      chips.push({
        rotulo: `Busca: "${this.termoBusca().trim()}"`,
        limpar: () => this.aoDigitarBusca('', true),
      });
    const marca = this.marcas().find((m) => m.id === this.filtroMarca());
    if (marca) chips.push({ rotulo: `Marca: ${marca.nome}`, limpar: () => this.aoMudarMarca('') });
    const categoria = this.categorias().find((c) => c.id === this.filtroCategoria());
    if (categoria)
      chips.push({
        rotulo: `Categoria: ${categoria.nome}`,
        limpar: () => this.aoMudarCategoria(''),
      });
    const subcategoria = this.subcategorias().find((s) => s.id === this.filtroSubcategoria());
    if (subcategoria)
      chips.push({
        rotulo: `Subcategoria: ${subcategoria.nome}`,
        limpar: () => this.aoMudarSubcategoria(''),
      });
    if (this.filtroStatus())
      chips.push({
        rotulo: `Status: ${this.statusLabel(this.filtroStatus() as StatusProduto)}`,
        limpar: () => this.aoMudarStatus(''),
      });
    return chips;
  });

  ngOnInit(): void {
    // pageSize alto porque são listas de apoio para os selects de filtro
    this.marcaService.listar(0, 500).subscribe((m) => this.marcas.set(m.filter((x) => x.nome)));
    this.categoriaService
      .listar(0, 500)
      .subscribe((c) => this.categorias.set(c.filter((x) => x.nome)));
    this.subcategoriaService.listar().subscribe((s) => this.subcategorias.set(s));
    this.carregar();
  }

  ngOnDestroy(): void {
    clearTimeout(this.timerBusca);
  }

  private filtroAtual(): FiltroProduto {
    return {
      nome: this.termoBusca(),
      marcaId: this.filtroMarca(),
      categoriaId: this.filtroCategoria(),
      subcategoriaId: this.filtroSubcategoria(),
      status: this.filtroStatus(),
    };
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    const filtro = this.filtroAtual();
    forkJoin({
      lista: this.produtoService.listar(filtro, this.pagina(), this.tamanhoPagina()),
      total: this.produtoService.contar(filtro),
    }).subscribe({
      next: ({ lista, total }) => {
        this.produtos.set(lista);
        this.total.set(total);
        this.carregando.set(false);
      },
      error: () => {
        this.erro.set('Não foi possível carregar os produtos. Verifique se a API está no ar.');
        this.carregando.set(false);
      },
    });
  }

  /** Toda mudança de filtro volta para a primeira página. */
  private aplicarFiltro(): void {
    this.pagina.set(0);
    this.carregar();
  }

  aoDigitarBusca(valor: string, imediato = false): void {
    this.termoBusca.set(valor);
    clearTimeout(this.timerBusca);
    if (imediato) this.aplicarFiltro();
    else this.timerBusca = setTimeout(() => this.aplicarFiltro(), 350);
  }

  aoMudarMarca(valor: string): void {
    this.filtroMarca.set(valor ? Number(valor) : null);
    this.aplicarFiltro();
  }

  aoMudarCategoria(valor: string): void {
    this.filtroCategoria.set(valor ? Number(valor) : null);
    // a subcategoria escolhida pode não pertencer à nova categoria
    const sub = this.subcategorias().find((s) => s.id === this.filtroSubcategoria());
    if (sub && sub.categoriaId !== this.filtroCategoria()) this.filtroSubcategoria.set(null);
    this.aplicarFiltro();
  }

  aoMudarSubcategoria(valor: string): void {
    this.filtroSubcategoria.set(valor ? Number(valor) : null);
    this.aplicarFiltro();
  }

  aoMudarStatus(valor: string): void {
    this.filtroStatus.set(valor as StatusProduto | '');
    this.aplicarFiltro();
  }

  limparFiltros(): void {
    clearTimeout(this.timerBusca);
    this.termoBusca.set('');
    this.filtroMarca.set(null);
    this.filtroCategoria.set(null);
    this.filtroSubcategoria.set(null);
    this.filtroStatus.set('');
    this.aplicarFiltro();
  }

  irParaPagina(p: number): void {
    if (p < 0 || p >= this.totalPaginas() || p === this.pagina()) return;
    this.pagina.set(p);
    this.carregar();
  }

  aoMudarTamanhoPagina(valor: string): void {
    this.tamanhoPagina.set(Number(valor));
    this.aplicarFiltro();
  }

  excluir(evento: Event, produto: Produto): void {
    evento.preventDefault();
    evento.stopPropagation();
    if (!confirm(`Excluir o produto "${produto.nome}"? Essa ação não pode ser desfeita.`)) return;

    this.produtoService.excluir(produto.id).subscribe({
      next: () => {
        // se era o último item da página, volta uma página
        if (this.produtos().length === 1 && this.pagina() > 0) this.pagina.update((p) => p - 1);
        this.carregar();
      },
      error: () => alert('Não foi possível excluir este produto.'),
    });
  }

  formatarPreco(valor: number): string {
    return this.formatoMoeda.format(valor);
  }

  statusLabel(status: StatusProduto): string {
    return { RASCUNHO: 'Rascunho', ATIVO: 'Ativo', INATIVO: 'Inativo' }[status];
  }

  corStatus(status: StatusProduto): string {
    return { ATIVO: 'var(--ok)', INATIVO: 'var(--dim2)', RASCUNHO: 'var(--warn)' }[status];
  }
}
