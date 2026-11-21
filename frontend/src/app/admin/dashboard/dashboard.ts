import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DashboardService } from '../../core/services/dashboard.service';
import { PedidoService } from '../../core/services/pedido.service';
import { FiltroDashboard, IndicadoresDashboard } from '../../core/models/dashboard.model';
import {
  EXIBICAO_STATUS_PEDIDO,
  PedidoListagem,
  STATUS_PEDIDO,
} from '../../core/models/pedido.model';

type Periodo = '30' | '7' | 'ano';

/** Quantos produtos de estoque baixo o card mostra; o resto fica no atalho para o estoque. */
const ESTOQUE_NO_CARD = 5;
const PENDENTES_NO_CARD = 5;

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, DatePipe],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard implements OnInit {
  private readonly dashboardService = inject(DashboardService);
  private readonly pedidoService = inject(PedidoService);

  readonly periodos: { rotulo: string; valor: Periodo }[] = [
    { rotulo: 'Últimos 30 dias', valor: '30' },
    { rotulo: 'Últimos 7 dias', valor: '7' },
    { rotulo: 'Este ano', valor: 'ano' },
  ];

  readonly periodo = signal<Periodo>('30');
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly dados = signal<IndicadoresDashboard | null>(null);
  /** Fila de pedidos aguardando pagamento, sem período (GET /pedidos?idStatus=1). */
  readonly pendentes = signal<PedidoListagem[]>([]);
  readonly totalPendentes = signal(0);

  readonly rotuloPeriodo = computed(
    () => this.periodos.find((p) => p.valor === this.periodo())?.rotulo ?? '',
  );
  readonly intervalo = computed(() => {
    const p = this.dados()?.periodo;
    return p?.dataInicio && p?.dataFim ? `${this.br(p.dataInicio)} a ${this.br(p.dataFim)}` : '';
  });

  readonly pedidosPorStatus = computed(() =>
    (this.dados()?.pedidosPorStatus ?? []).map((r) => ({
      id: r.status.id,
      rotulo: EXIBICAO_STATUS_PEDIDO[r.status.id]?.rotulo ?? r.status.nome,
      cor: EXIBICAO_STATUS_PEDIDO[r.status.id]?.cor ?? 'var(--mute)',
      quantidade: r.quantidade,
    })),
  );
  /** Largura da barra de cada status, relativa ao maior (só apresentação). */
  readonly maiorStatus = computed(() =>
    Math.max(1, ...this.pedidosPorStatus().map((s) => s.quantidade)),
  );
  readonly totalPedidosPeriodo = computed(() =>
    this.pedidosPorStatus().reduce((soma, s) => soma + s.quantidade, 0),
  );

  readonly estoqueBaixo = computed(() => this.dados()?.estoqueBaixo ?? []);
  readonly estoqueNoCard = computed(() => this.estoqueBaixo().slice(0, ESTOQUE_NO_CARD));
  readonly estoqueRestante = computed(() =>
    Math.max(0, this.estoqueBaixo().length - ESTOQUE_NO_CARD),
  );

  readonly ultimosPedidos = computed(() => this.dados()?.ultimosPedidos ?? []);

  /** Aguardando ação (PENDENTE, PAGO, SEPARACAO) vem da API; sem o campo, cai nos pendentes. */
  readonly aguardando = computed(() => this.dados()?.pedidosAguardandoAcao ?? this.pendentes());
  readonly totalAguardando = computed(
    () => this.dados()?.quantidadeAguardandoAcao ?? this.totalPendentes(),
  );
  readonly maisVendidos = computed(() => this.dados()?.maisVendidos ?? []);
  readonly receitaPorCategoria = computed(() => this.dados()?.receitaPorCategoria ?? []);
  readonly metrica = signal<'receita' | 'pedidos'>('receita');
  readonly exportando = signal(false);

  /** Série do gráfico: período atual e anterior na mesma escala (0 a 100 de altura). */
  readonly grafico = computed(() => {
    const dias = this.dados()?.vendasPorDia ?? [];
    const receita = this.metrica() === 'receita';
    const atual = dias.map((d) => (receita ? d.faturamento : d.quantidadePedidos));
    const anterior = dias.map((d) => (receita ? d.faturamentoAnterior : d.quantidadePedidosAnterior));
    const maior = Math.max(1, ...atual, ...anterior);
    const passo = dias.length > 1 ? 700 / (dias.length - 1) : 0;
    const linha = (v: number[]) =>
      v.map((n, i) => `${i ? 'L' : 'M'}${(44 + i * passo).toFixed(1)} ${(212 - (n / maior) * 198).toFixed(1)}`).join(' ');
    const marcas = dias
      .map((d, i) => ({ x: 44 + i * passo, rotulo: this.br(d.data).slice(0, 5) }))
      .filter((_, i) => i % Math.max(1, Math.ceil(dias.length / 5)) === 0);
    return { atual: linha(atual), anterior: linha(anterior), marcas, vazio: !dias.length };
  });

  variacao(v: number | null | undefined): string {
    if (v === null || v === undefined) return '';
    const n = new Intl.NumberFormat('pt-BR', { minimumFractionDigits: 1, maximumFractionDigits: 1 }).format(Math.abs(v));
    return `${v >= 0 ? '▲ +' : '▼ −'}${n}%`;
  }

  exportar(): void {
    this.exportando.set(true);
    this.dashboardService.exportar(this.filtro()).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'vendas-por-dia.csv';
        a.click();
        URL.revokeObjectURL(url);
        this.exportando.set(false);
      },
      error: () => {
        this.erro.set('Não foi possível exportar o relatório.');
        this.exportando.set(false);
      },
    });
  }

  private readonly formatoMoeda = new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  });

  ngOnInit(): void {
    this.carregar();
    this.carregarPendentes();
  }

  carregarPendentes(): void {
    this.pedidoService
      .listar({ idStatus: STATUS_PEDIDO.PENDENTE }, 0, PENDENTES_NO_CARD)
      .subscribe({
        next: (res) => {
          this.pendentes.set(res.items ?? []);
          this.totalPendentes.set(res.totalItems ?? 0);
        },
        error: () => {
          this.pendentes.set([]);
          this.totalPendentes.set(0);
        },
      });
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.dashboardService.buscar(this.filtro()).subscribe({
      next: (d) => {
        this.dados.set(d);
        this.carregando.set(false);
      },
      error: () => {
        this.erro.set('Erro ao carregar os indicadores.');
        this.carregando.set(false);
      },
    });
  }

  aoMudarPeriodo(valor: string): void {
    this.periodo.set(valor as Periodo);
    this.carregar();
  }

  moeda(valor: number | null | undefined): string {
    return this.formatoMoeda.format(valor ?? 0);
  }

  rotuloStatus(id: number): string {
    return EXIBICAO_STATUS_PEDIDO[id]?.rotulo ?? '';
  }

  corStatus(id: number): string {
    return EXIBICAO_STATUS_PEDIDO[id]?.cor ?? 'var(--mute)';
  }

  /** 30 dias é o padrão da API, então vai sem datas; os outros períodos terminam hoje. */
  private filtro(): FiltroDashboard {
    const hoje = new Date();
    switch (this.periodo()) {
      case '7':
        return {
          dataInicio: this.iso(new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate() - 7)),
          dataFim: this.iso(hoje),
        };
      case 'ano':
        return { dataInicio: `${hoje.getFullYear()}-01-01`, dataFim: this.iso(hoje) };
      default:
        return {};
    }
  }

  private iso(d: Date): string {
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${d.getFullYear()}-${mm}-${dd}`;
  }

  /** AAAA-MM-DD -> DD/MM/AAAA sem passar por Date (evita o deslocamento de fuso). */
  private br(data: string): string {
    const [a, m, d] = data.split('-');
    return `${d}/${m}/${a}`;
  }
}
