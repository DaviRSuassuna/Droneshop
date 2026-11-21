import { ItemEstoque } from './estoque.model';
import { PedidoListagem, ResumoStatusPedido } from './pedido.model';

/** Produto abaixo do estoque mínimo (mesma regra de GET /estoque?situacao=ABAIXO_MINIMO). */
export interface EstoqueBaixo {
  produto: ItemEstoque['produto'];
  disponivel: number;
  estoqueMinimo: number;
}

/**
 * GET /admin/dashboard (#18). Tudo calculado no backend. Faturamento e
 * quantidadePedidos contam os pedidos pagos, em separação, enviados e entregues
 * do período; estoqueBaixo é o saldo de agora.
 */
export interface IndicadoresDashboard {
  periodo: { dataInicio: string; dataFim: string };
  faturamento: number;
  quantidadePedidos: number;
  ticketMedio: number;
  pedidosPorStatus: ResumoStatusPedido[];
  estoqueBaixo: EstoqueBaixo[];
  ultimosPedidos: PedidoListagem[];
  /** Indicadores da issue nova-dashboard-indicadores (#85). */
  comparacao?: ComparacaoPeriodo;
  vendasPorDia?: VendaDia[];
  quantidadeAguardandoAcao?: number;
  pedidosAguardandoAcao?: PedidoListagem[];
  maisVendidos?: MaisVendido[];
  receitaPorCategoria?: ReceitaCategoria[];
}

/** Período anterior de mesmo tamanho; variação em %, nula sem base. */
export interface ComparacaoPeriodo {
  periodoAnterior: { dataInicio: string; dataFim: string };
  faturamento: number;
  quantidadePedidos: number;
  ticketMedio: number;
  variacaoFaturamento: number | null;
  variacaoPedidos: number | null;
  variacaoTicketMedio: number | null;
}

export interface VendaDia {
  data: string;
  faturamento: number;
  quantidadePedidos: number;
  faturamentoAnterior: number;
  quantidadePedidosAnterior: number;
}

export interface MaisVendido {
  produto: { id: number; nome: string; sku: string };
  unidades: number;
  receita: number;
  participacao: number;
}

export interface ReceitaCategoria {
  categoria: string;
  receita: number;
  participacao: number;
}

/** Datas AAAA-MM-DD, inclusivas; sem datas, a API usa os últimos 30 dias. */
export interface FiltroDashboard {
  dataInicio?: string | null;
  dataFim?: string | null;
}
