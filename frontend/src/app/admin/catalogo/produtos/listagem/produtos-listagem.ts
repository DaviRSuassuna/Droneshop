import { SubcategoriaService } from '../../../../core/services/subcategoria.service';
import { Subcategoria } from '../../../../core/models/subcategoria.model';
import { baixarArquivo } from '../../../../shared/arquivo';
import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { ProdutoService } from '../../../../core/services/produto.service';
import { MarcaService } from '../../../../core/services/marca.service';
import { GrupoProdutoService } from '../../../../core/services/grupo-produto.service';
import {
  FiltroProduto,
  Produto,
  ProdutoLoteRequest,
  TIPO_PRODUTO,
} from '../../../../core/models/produto.model';
import { STATUS_PRODUTO } from '../../../../core/models/status.model';
import { Marca } from '../../../../core/models/marca.model';
import { GrupoProduto } from '../../../../core/models/grupo-produto.model';
import { ProblemDetail } from '../../../../core/models/problem.model';

type FiltroEstoque = 'ABAIXO_MINIMO' | 'ZERADO';

/**
 * Listagem de produtos (drone, acessório e kit, D40) com filtro e paginação no servidor:
 * cada mudança dispara GET /produtos, que devolve a página e o total (PageResponse).
 * Ações em lote reutilizam as validações do cadastro e informam as falhas por produto.
 */
@Component({
  selector: 'app-produtos-listagem',
  imports: [RouterLink],
  templateUrl: './produtos-listagem.html',
})
export class ProdutosListagem implements OnInit, OnDestroy {
  private readonly produtoService = inject(ProdutoService);
  private readonly marcaService = inject(MarcaService);
  private readonly grupoService = inject(GrupoProdutoService);
  private readonly router = inject(Router);
  private readonly subcategoriaService = inject(SubcategoriaService);
  readonly subcategorias = signal<Subcategoria[]>([]);
  readonly selecionados = signal<Set<number>>(new Set());
  readonly processandoLote = signal(false);
  readonly resultadoLote = signal('');
  readonly falhasLote = signal<{ id: number; motivo: string }[]>([]);
  readonly acaoPendente = signal<'REAJUSTAR_PRECO' | 'MOVER_SUBCATEGORIA' | null>(null);
  readonly percentualLote = signal('');
  readonly subcategoriaLote = signal('');
  readonly todosSelecionados = computed(
    () => this.produtos().length > 0 && this.produtos().every((p) => this.selecionados().has(p.id)),
  );

  readonly STATUS = STATUS_PRODUTO;
  readonly TIPO = TIPO_PRODUTO;
  readonly tamanhoPagina = 10;

  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly produtos = signal<Produto[]>([]);
  readonly total = signal(0);
  readonly rascunhos = signal(0);
  readonly totalGrupos = signal(0);

  // opções dos selects
  readonly marcas = signal<Marca[]>([]);
  readonly grupos = signal<GrupoProduto[]>([]);

  // filtros
  readonly termoBusca = signal('');
  readonly filtroTipo = signal<number | null>(null);
  readonly filtroGrupo = signal<number | null>(null);
  readonly filtroMarca = signal<number | null>(null);
  readonly filtroStatus = signal<number | null>(null);
  readonly filtroEstoque = signal<FiltroEstoque | null>(null);

  readonly pagina = signal(0);

  private readonly formatoMoeda = new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
    maximumFractionDigits: 0,
  });

  private timerBusca?: ReturnType<typeof setTimeout>;

  readonly totalPaginas = computed(() => Math.max(1, Math.ceil(this.total() / this.tamanhoPagina)));

  readonly exibindo = computed(() => {
    if (this.total() === 0) return 'Exibindo 0 de 0';
    const inicio = this.pagina() * this.tamanhoPagina + 1;
    const fim = Math.min(inicio + this.produtos().length - 1, this.total());
    return `Exibindo ${inicio} a ${fim} de ${this.total()}`;
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
    const tipo = this.filtroTipo();
    if (tipo)
      chips.push({
        rotulo: `Tipo: ${nomeTipo(tipo)}`,
        limpar: () => this.filtrar(this.filtroTipo, null),
      });
    const grupo = this.grupos().find((g) => g.id === this.filtroGrupo());
    if (grupo)
      chips.push({
        rotulo: `Grupo: ${grupo.nome}`,
        limpar: () => this.filtrar(this.filtroGrupo, null),
      });
    const marca = this.marcas().find((m) => m.id === this.filtroMarca());
    if (marca)
      chips.push({
        rotulo: `Marca: ${marca.nome}`,
        limpar: () => this.filtrar(this.filtroMarca, null),
      });
    const status = this.filtroStatus();
    if (status)
      chips.push({
        rotulo: `Status: ${nomeStatus(status)}`,
        limpar: () => this.filtrar(this.filtroStatus, null),
      });
    const estoque = this.filtroEstoque();
    if (estoque)
      chips.push({
        rotulo: `Estoque: ${estoque === 'ZERADO' ? 'zerado' : 'abaixo do mínimo'}`,
        limpar: () => this.filtrar(this.filtroEstoque, null),
      });
    return chips;
  });

  ngOnInit(): void {
    this.subcategoriaService
      .listar(null, 0, 1000)
      .subscribe((p) => this.subcategorias.set(p.items.filter((sub) => sub.categoria !== null)));
    // pageSize alto porque são listas de apoio para os selects de filtro
    this.marcaService
      .listar(0, 1000)
      .subscribe((p) => this.marcas.set(p.items.filter((x) => x.nome)));
    this.grupoService.listar({}, 0, 1000).subscribe((p) => {
      this.grupos.set(p.items);
      this.totalGrupos.set(p.totalItems);
    });
    this.produtoService
      .listar({ idStatus: STATUS_PRODUTO.RASCUNHO }, 0, 1)
      .subscribe((p) => this.rascunhos.set(p.totalItems));
    this.carregar();
  }

  ngOnDestroy(): void {
    clearTimeout(this.timerBusca);
  }

  private filtroAtual(): FiltroProduto {
    return {
      busca: this.termoBusca(),
      idTipo: this.filtroTipo(),
      idGrupo: this.filtroGrupo(),
      idMarca: this.filtroMarca(),
      idStatus: this.filtroStatus(),
      estoque: this.filtroEstoque(),
    };
  }

  carregar(): void {
    this.selecionados.set(new Set());
    this.carregando.set(true);
    this.erro.set(null);
    this.produtoService.listar(this.filtroAtual(), this.pagina(), this.tamanhoPagina).subscribe({
      next: (resposta) => {
        this.produtos.set(resposta.items);
        this.total.set(resposta.totalItems);
        this.carregando.set(false);
      },
      error: () => {
        this.erro.set('Não foi possível carregar os produtos.');
        this.carregando.set(false);
      },
    });
  }

  /** Busca com espera de 300 ms; imediato = true ao limpar pelo chip. */
  aoDigitarBusca(valor: string, imediato = false): void {
    this.termoBusca.set(valor);
    clearTimeout(this.timerBusca);
    const aplicar = () => {
      this.pagina.set(0);
      this.carregar();
    };
    if (imediato) aplicar();
    else this.timerBusca = setTimeout(aplicar, 300);
  }

  /** Os selects devolvem string; '' é "sem filtro". */
  aoMudar(filtro: 'tipo' | 'grupo' | 'marca' | 'status', valor: string): void {
    const alvo = {
      tipo: this.filtroTipo,
      grupo: this.filtroGrupo,
      marca: this.filtroMarca,
      status: this.filtroStatus,
    }[filtro];
    this.filtrar(alvo, valor ? Number(valor) : null);
  }

  aoMudarEstoque(valor: string): void {
    this.filtrar(this.filtroEstoque, (valor || null) as FiltroEstoque | null);
  }

  private filtrar<T>(alvo: { set(v: T): void }, valor: T): void {
    alvo.set(valor);
    this.pagina.set(0);
    this.carregar();
  }

  limparFiltros(): void {
    clearTimeout(this.timerBusca);
    this.termoBusca.set('');
    this.filtroTipo.set(null);
    this.filtroGrupo.set(null);
    this.filtroMarca.set(null);
    this.filtroStatus.set(null);
    this.filtroEstoque.set(null);
    this.pagina.set(0);
    this.carregar();
  }

  irParaPagina(p: number): void {
    if (p < 0 || p >= this.totalPaginas() || p === this.pagina()) return;
    this.pagina.set(p);
    this.carregar();
  }

  abrir(produto: Produto): void {
    this.router.navigate(this.rotaEdicao(produto));
  }

  rotaEdicao(produto: Produto): (string | number)[] {
    return ['/admin', rotaDoTipo(produto.tipo.id), produto.id, 'editar'];
  }

  nomeTipo(id: number): string {
    return nomeTipo(id);
  }

  preco(valor: number | null): string {
    return valor === null ? '—' : this.formatoMoeda.format(valor);
  }

  estoqueBaixo(produto: Produto): boolean {
    return (
      (produto.estoqueFisico ?? 0) < (produto.estoqueMinimo ?? 0) ||
      (produto.estoqueFisico ?? 0) <= 0
    );
  }

  classeStatus(produto: Produto): string {
    if (produto.status.id === STATUS_PRODUTO.ATIVO) return 'ok';
    return produto.status.id === STATUS_PRODUTO.INATIVO ? 'warn' : 'mute';
  }

  selecionar(id: number, marcado: boolean): void {
    this.selecionados.update((ids) => {
      const novo = new Set(ids);
      if (marcado) novo.add(id);
      else novo.delete(id);
      return novo;
    });
  }

  selecionarTodos(marcado: boolean): void {
    this.selecionados.set(new Set(marcado ? this.produtos().map((p) => p.id) : []));
  }

  aplicarLote(acao: ProdutoLoteRequest['acao']): void {
    if (this.processandoLote() || !this.selecionados().size) return;
    const dto: ProdutoLoteRequest = { ids: [...this.selecionados()], acao };
    if (acao === 'REAJUSTAR_PRECO') {
      const valor = this.percentualLote().trim().replace(',', '.');
      if (!valor || !Number.isFinite(Number(valor))) {
        this.erro.set('Informe um percentual válido.');
        return;
      }
      dto.percentual = Number(valor);
    }
    if (acao === 'MOVER_SUBCATEGORIA') {
      if (!this.subcategoriaLote()) {
        this.erro.set('Selecione a subcategoria de destino.');
        return;
      }
      dto.idSubcategoria = Number(this.subcategoriaLote());
    }
    this.processandoLote.set(true);
    this.falhasLote.set([]);
    this.resultadoLote.set('');
    this.produtoService.alterarEmLote(dto).subscribe({
      next: (resultado) => {
        this.processandoLote.set(false);
        this.acaoPendente.set(null);
        this.resultadoLote.set(resultado.processados + ' produto(s) processado(s).');
        this.falhasLote.set(resultado.falhas);
        this.carregar();
        this.produtoService
          .listar({ idStatus: STATUS_PRODUTO.RASCUNHO }, 0, 1)
          .subscribe((p) => this.rascunhos.set(p.totalItems));
      },
      error: (err: HttpErrorResponse) => {
        this.processandoLote.set(false);
        this.erro.set(
          (err.error as ProblemDetail | undefined)?.detail ?? 'Não foi possível processar o lote.',
        );
      },
    });
  }

  excluir(evento: Event, produto: Produto): void {
    evento.stopPropagation();
    if (
      !confirm(`Excluir "${produto.nome ?? 'produto sem nome'}"? Essa ação não pode ser desfeita.`)
    )
      return;
    this.produtoService.excluir(produto.id).subscribe({
      next: () => this.carregar(),
      // 409: produto em uso, como componente de kit (D24); o detail explica e sugere inativar
      error: (err: HttpErrorResponse) =>
        alert(
          (err.error as ProblemDetail | undefined)?.detail ??
            'Não foi possível excluir este produto.',
        ),
    });
  }

  /** Exporta o resultado filtrado inteiro (todos os itens), não só a página. */
  exportarCsv(): void {
    this.produtoService.exportar(this.filtroAtual()).subscribe({
      next: (blob) => baixarArquivo('produtos.csv', blob),
      error: () => this.erro.set('Não foi possível exportar o CSV.'),
    });
  }
}

function nomeTipo(id: number): string {
  return id === TIPO_PRODUTO.DRONE ? 'Drone' : id === TIPO_PRODUTO.KIT ? 'Kit' : 'Acessório';
}

function rotaDoTipo(id: number): string {
  return id === TIPO_PRODUTO.DRONE ? 'drones' : id === TIPO_PRODUTO.KIT ? 'kits' : 'acessorios';
}

function nomeStatus(id: number): string {
  return id === STATUS_PRODUTO.ATIVO
    ? 'Ativo'
    : id === STATUS_PRODUTO.INATIVO
      ? 'Inativo'
      : 'Rascunho';
}
