import { baixarArquivo } from '../../../shared/arquivo';
import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { EstoqueService } from '../../../core/services/estoque.service';
import { MarcaService } from '../../../core/services/marca.service';
import { Marca } from '../../../core/models/marca.model';
import { TIPO_PRODUTO } from '../../../core/models/produto.model';
import {
  EXIBICAO_TIPO_MOVIMENTO,
  ItemEstoque,
  MovimentoEstoque,
  SituacaoEstoque,
  TIPO_MOVIMENTO,
} from '../../../core/models/estoque.model';

const TAMANHO_PAGINA = 10;

type Aba = 'saldo' | 'movimentos';

/**
 * Estoque (#13, D40): saldo por produto e movimentos. Reservas (#23) e números de série (#34)
 * ainda não têm API e ficam de fora (ver e2e/conformidade/telas.ts).
 */
@Component({
  selector: 'app-estoque-listagem',
  imports: [RouterLink, DatePipe],
  templateUrl: './estoque-listagem.html',
})
export class EstoqueListagem implements OnInit, OnDestroy {
  private readonly estoqueService = inject(EstoqueService);
  private readonly marcaService = inject(MarcaService);

  readonly situacoes: { rotulo: string; valor: SituacaoEstoque }[] = [
    { rotulo: 'Situação', valor: 'TODOS' },
    { rotulo: 'Abaixo do mínimo', valor: 'ABAIXO_MINIMO' },
    { rotulo: 'Sem estoque', valor: 'ZERADO' },
  ];
  readonly tiposMovimento = Object.values(TIPO_MOVIMENTO).map((id) => ({
    id,
    rotulo: EXIBICAO_TIPO_MOVIMENTO[id].rotulo,
  }));

  readonly aba = signal<Aba>('saldo');
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);

  readonly itens = signal<ItemEstoque[]>([]);
  readonly movimentos = signal<MovimentoEstoque[]>([]);
  readonly termoBusca = signal('');
  readonly situacao = signal<SituacaoEstoque>('TODOS');
  readonly idMarca = signal<number | null>(null);
  readonly marcas = signal<Marca[]>([]);
  readonly idTipo = signal<number | null>(null);
  readonly pagina = signal(0);
  readonly totalItens = signal(0);
  readonly totalPaginasApi = signal(0);

  // contagens do cabeçalho e das abas, sem os filtros da tela
  readonly totalProdutos = signal(0);
  readonly totalAbaixo = signal(0);
  readonly totalZerado = signal(0);
  readonly totalMovimentos = signal(0);

  readonly totalPaginas = computed(() => Math.max(1, this.totalPaginasApi()));
  readonly intervaloExibido = computed(() => {
    if (this.totalItens() === 0) return '0 a 0';
    const exibidos = this.aba() === 'saldo' ? this.itens().length : this.movimentos().length;
    const inicio = this.pagina() * TAMANHO_PAGINA + 1;
    return `${inicio} a ${Math.min(inicio + exibidos - 1, this.totalItens())}`;
  });

  private buscaTimeout: ReturnType<typeof setTimeout> | undefined;

  ngOnInit(): void {
    this.marcaService.listar(0, 1000).subscribe((p) => this.marcas.set(p.items.filter((x) => x.nome)));
    this.carregarResumo();
    this.carregar();
  }

  ngOnDestroy(): void {
    if (this.buscaTimeout !== undefined) clearTimeout(this.buscaTimeout);
  }

  carregarResumo(): void {
    forkJoin({
      todos: this.estoqueService.listar({}, 0, 1),
      abaixo: this.estoqueService.listar({ situacao: 'ABAIXO_MINIMO' }, 0, 1),
      zerado: this.estoqueService.listar({ situacao: 'ZERADO' }, 0, 1),
      movimentos: this.estoqueService.listarMovimentos({}, 0, 1),
    }).subscribe({
      next: (r) => {
        this.totalProdutos.set(r.todos.totalItems);
        this.totalAbaixo.set(r.abaixo.totalItems);
        this.totalZerado.set(r.zerado.totalItems);
        this.totalMovimentos.set(r.movimentos.totalItems);
      },
      error: () => {
        // só as contagens ficam zeradas; a tabela mostra o próprio erro
      },
    });
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    const fim = {
      error: () => {
        this.erro.set(
          this.aba() === 'saldo'
            ? 'Erro ao carregar o estoque.'
            : 'Erro ao carregar os movimentos.',
        );
        this.carregando.set(false);
      },
    };
    if (this.aba() === 'saldo') {
      this.estoqueService
        .listar(
          { situacao: this.situacao(), busca: this.termoBusca(), idMarca: this.idMarca() },
          this.pagina(),
          TAMANHO_PAGINA,
        )
        .subscribe({
          next: (res) => {
            this.itens.set(res.items);
            this.aplicarPagina(res.totalItems, res.totalPages);
          },
          ...fim,
        });
    } else {
      this.estoqueService
        .listarMovimentos({ idTipo: this.idTipo() }, this.pagina(), TAMANHO_PAGINA)
        .subscribe({
          next: (res) => {
            this.movimentos.set(res.items);
            this.aplicarPagina(res.totalItems, res.totalPages);
          },
          ...fim,
        });
    }
  }

  selecionarAba(evento: Event, aba: Aba): void {
    evento.preventDefault();
    if (this.aba() === aba) return;
    this.aba.set(aba);
    this.pagina.set(0);
    this.carregar();
  }

  aoDigitarBusca(valor: string): void {
    this.termoBusca.set(valor);
    this.pagina.set(0);
    if (this.buscaTimeout !== undefined) clearTimeout(this.buscaTimeout);
    this.buscaTimeout = setTimeout(() => this.carregar(), 300);
  }

  aoMudarSituacao(valor: string): void {
    this.situacao.set(valor as SituacaoEstoque);
    this.pagina.set(0);
    this.carregar();
  }

  aoMudarMarca(valor: string): void {
    this.idMarca.set(valor ? Number(valor) : null);
    this.pagina.set(0);
    this.carregar();
  }

  aoMudarTipo(valor: string): void {
    this.idTipo.set(valor ? Number(valor) : null);
    this.pagina.set(0);
    this.carregar();
  }

  irParaPagina(p: number): void {
    if (p < 0 || p >= this.totalPaginas() || p === this.pagina()) return;
    this.pagina.set(p);
    this.carregar();
  }

  ehKit(item: ItemEstoque): boolean {
    return item.produto.tipo.id === TIPO_PRODUTO.KIT;
  }

  nomeProduto(item: ItemEstoque): string {
    const nome = item.produto.nome ?? 'Produto sem nome';
    return this.ehKit(item) ? `${nome} · Kit` : nome;
  }

  /** Status com ponto e texto (DESIGN.md): kit em info, sem estoque em warn, abaixo do mínimo em âmbar. */
  situacaoDoItem(item: ItemEstoque): { rotulo: string; classe: string } {
    if (this.ehKit(item)) return { rotulo: 'Kit', classe: 'st info' };
    if (item.disponivel <= 0) return { rotulo: 'Sem estoque', classe: 'st warn' };
    if (item.abaixoDoMinimo) return { rotulo: 'Abaixo do mínimo', classe: 'st amber' };
    return { rotulo: 'Saudável', classe: 'st ok' };
  }

  /** Exporta o saldo filtrado inteiro (todos os produtos), não só a página. */
  exportarCsv(): void {
    this.estoqueService.exportar({ situacao: this.situacao(), busca: this.termoBusca(), idMarca: this.idMarca() }).subscribe({
      next: (blob) => baixarArquivo('estoque.csv', blob),
      error: () => this.erro.set('Não foi possível exportar o CSV.'),
    });
  }

  rotuloTipo(m: MovimentoEstoque): string {
    const rotulo = EXIBICAO_TIPO_MOVIMENTO[m.tipo.id]?.rotulo ?? m.tipo.nome;
    return m.observacao ? `${rotulo} · ${m.observacao}` : rotulo;
  }

  /** Variação do saldo com sinal; no ajuste a API já devolve a diferença. */
  variacao(m: MovimentoEstoque): string {
    const saida = m.tipo.id === TIPO_MOVIMENTO.SAIDA || m.tipo.id === TIPO_MOVIMENTO.PERDA;
    const valor = saida ? -m.quantidade : m.quantidade;
    return valor > 0 ? `+${valor}` : `${valor}`;
  }

  autor(m: MovimentoEstoque): string {
    if (m.autor) return m.autor.nome;
    return m.origem.nome === 'INTEGRACAO' ? 'Integração' : 'Sistema';
  }

  private aplicarPagina(totalItens: number, totalPaginas: number): void {
    this.totalItens.set(totalItens);
    this.totalPaginasApi.set(totalPaginas);
    this.carregando.set(false);
  }
}
