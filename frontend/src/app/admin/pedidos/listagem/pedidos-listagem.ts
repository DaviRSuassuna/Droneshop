import { baixarArquivo } from '../../../shared/arquivo';
import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { PedidoService } from '../../../core/services/pedido.service';
import {
  EXIBICAO_STATUS_PEDIDO,
  PedidoListagem,
  ResumoStatusPedido,
  STATUS_PEDIDO,
} from '../../../core/models/pedido.model';

const TAMANHO_PAGINA = 10;

/** Abas do protótipo; "Todos" mostra também Em devolução e Devolvido. */
const ABAS: { rotulo: string; idStatus: number | null }[] = [
  { rotulo: 'Todos', idStatus: null },
  { rotulo: 'Aguardando pagamento', idStatus: STATUS_PEDIDO.PENDENTE },
  { rotulo: 'Pagos', idStatus: STATUS_PEDIDO.PAGO },
  { rotulo: 'Em separação', idStatus: STATUS_PEDIDO.SEPARACAO },
  { rotulo: 'Enviados', idStatus: STATUS_PEDIDO.ENVIADO },
  { rotulo: 'Entregues', idStatus: STATUS_PEDIDO.ENTREGUE },
  { rotulo: 'Cancelados', idStatus: STATUS_PEDIDO.CANCELADO },
];

@Component({
  selector: 'app-pedidos-listagem',
  imports: [RouterLink, DatePipe],
  templateUrl: './pedidos-listagem.html',
  styleUrl: './pedidos-listagem.scss',
})
export class PedidosListagem implements OnInit, OnDestroy {
  private readonly pedidoService = inject(PedidoService);

  readonly abas = ABAS;
  readonly STATUS = STATUS_PEDIDO;
  readonly periodos = [
    { rotulo: 'Período: todos', dias: 0 },
    { rotulo: 'Período: 7 dias', dias: 7 },
    { rotulo: 'Período: 30 dias', dias: 30 },
    { rotulo: 'Período: 90 dias', dias: 90 },
    { rotulo: 'Período: 12 meses', dias: 365 },
  ];

  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly pedidos = signal<PedidoListagem[]>([]);
  readonly resumo = signal<ResumoStatusPedido[]>([]);

  readonly aba = signal<number | null>(null);
  readonly termoBusca = signal('');
  readonly periodoDias = signal(0);
  readonly pagina = signal(0);
  readonly totalItens = signal(0);
  readonly totalPaginasApi = signal(0);

  readonly totalPaginas = computed(() => Math.max(1, this.totalPaginasApi()));
  readonly intervaloExibido = computed(() => {
    if (this.totalItens() === 0) return '0 a 0';
    const inicio = this.pagina() * TAMANHO_PAGINA + 1;
    return `${inicio} a ${Math.min(inicio + this.pedidos().length - 1, this.totalItens())}`;
  });

  private readonly contagem = computed(() => {
    const mapa = new Map<number, number>();
    for (const r of this.resumo()) mapa.set(r.status.id, r.quantidade);
    return mapa;
  });
  readonly totalGeral = computed(() => [...this.contagem().values()].reduce((a, b) => a + b, 0));
  /** Pagos e em separação: o que o admin ainda precisa separar e enviar. */
  readonly aguardandoAcao = computed(
    () =>
      (this.contagem().get(STATUS_PEDIDO.PAGO) ?? 0) +
      (this.contagem().get(STATUS_PEDIDO.SEPARACAO) ?? 0),
  );

  private readonly formatoMoeda = new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  });
  private buscaTimeout: ReturnType<typeof setTimeout> | undefined;

  ngOnInit(): void {
    this.carregarResumo();
    this.carregar();
  }

  ngOnDestroy(): void {
    if (this.buscaTimeout !== undefined) clearTimeout(this.buscaTimeout);
  }

  carregarResumo(): void {
    this.pedidoService.resumoStatus().subscribe({
      next: (r) => this.resumo.set(Array.isArray(r) ? r : []),
      error: () => this.resumo.set([]),
    });
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    const periodo = this.periodo();
    this.pedidoService
      .listar(
        {
          idStatus: this.aba(),
          busca: this.termoBusca(),
          dataInicio: periodo?.inicio,
          dataFim: periodo?.fim,
        },
        this.pagina(),
        TAMANHO_PAGINA,
      )
      .subscribe({
        next: (res) => {
          this.pedidos.set(res.items);
          this.totalItens.set(res.totalItems);
          this.totalPaginasApi.set(res.totalPages);
          this.carregando.set(false);
        },
        error: () => {
          this.erro.set('Erro ao carregar pedidos.');
          this.carregando.set(false);
        },
      });
  }

  contagemDaAba(idStatus: number | null): number {
    return idStatus === null ? this.totalGeral() : (this.contagem().get(idStatus) ?? 0);
  }

  selecionarAba(evento: Event, idStatus: number | null): void {
    evento.preventDefault();
    if (this.aba() === idStatus) return;
    this.aba.set(idStatus);
    this.pagina.set(0);
    this.carregar();
  }

  aoDigitarBusca(valor: string): void {
    this.termoBusca.set(valor);
    this.pagina.set(0);
    if (this.buscaTimeout !== undefined) clearTimeout(this.buscaTimeout);
    this.buscaTimeout = setTimeout(() => this.carregar(), 300);
  }

  aoMudarPeriodo(valor: string): void {
    this.periodoDias.set(Number(valor));
    this.pagina.set(0);
    this.carregar();
  }

  irParaPagina(p: number): void {
    if (p < 0 || p >= this.totalPaginas() || p === this.pagina()) return;
    this.pagina.set(p);
    this.carregar();
  }

  exportarCsv(): void {
    const periodo = this.periodo();
    this.pedidoService
      .exportar({
        idStatus: this.aba(),
        busca: this.termoBusca(),
        dataInicio: periodo?.inicio,
        dataFim: periodo?.fim,
      })
      .subscribe({
        next: (blob) => baixarArquivo('pedidos.csv', blob),
        error: () => this.erro.set('Não foi possível exportar os pedidos.'),
      });
  }

  rotuloStatus(id: number): string {
    return EXIBICAO_STATUS_PEDIDO[id]?.rotulo ?? '';
  }

  corStatus(id: number): string {
    return EXIBICAO_STATUS_PEDIDO[id]?.cor ?? 'var(--mute)';
  }

  moeda(valor: number): string {
    return this.formatoMoeda.format(valor ?? 0);
  }

  /** Últimos N dias até hoje, as duas pontas inclusivas (datas locais). */
  private periodo(): { inicio: string; fim: string } | null {
    const dias = this.periodoDias();
    if (!dias) return null;
    const hoje = new Date();
    const inicio = new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate() - dias);
    return { inicio: this.iso(inicio), fim: this.iso(hoje) };
  }

  private iso(d: Date): string {
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${d.getFullYear()}-${mm}-${dd}`;
  }
}
