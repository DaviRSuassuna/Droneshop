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
}

/** Datas AAAA-MM-DD, inclusivas; sem datas, a API usa os últimos 30 dias. */
export interface FiltroDashboard {
  dataInicio?: string | null;
  dataFim?: string | null;
}
