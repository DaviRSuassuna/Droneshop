import { StatusRef } from './status.model';

/** Ids de StatusPedido (D31, ordem do diagrama). */
export const STATUS_PEDIDO = {
  PENDENTE: 1,
  PAGO: 2,
  SEPARACAO: 3,
  ENVIADO: 4,
  ENTREGUE: 5,
  EM_DEVOLUCAO: 6,
  DEVOLVIDO: 7,
  CANCELADO: 8,
} as const;

/** Rótulo e cor do ponto de cada status, como no protótipo. */
export const EXIBICAO_STATUS_PEDIDO: Record<number, { rotulo: string; cor: string }> = {
  [STATUS_PEDIDO.PENDENTE]: { rotulo: 'Aguardando pagamento', cor: 'var(--amber)' },
  [STATUS_PEDIDO.PAGO]: { rotulo: 'Pago', cor: 'var(--ok)' },
  [STATUS_PEDIDO.SEPARACAO]: { rotulo: 'Em separação', cor: 'var(--info)' },
  [STATUS_PEDIDO.ENVIADO]: { rotulo: 'Enviado', cor: 'var(--info)' },
  [STATUS_PEDIDO.ENTREGUE]: { rotulo: 'Entregue', cor: 'var(--ok)' },
  [STATUS_PEDIDO.EM_DEVOLUCAO]: { rotulo: 'Em devolução', cor: 'var(--warn)' },
  [STATUS_PEDIDO.DEVOLVIDO]: { rotulo: 'Devolvido', cor: 'var(--mute)' },
  [STATUS_PEDIDO.CANCELADO]: { rotulo: 'Cancelado', cor: 'var(--warn)' },
};

/** Transições permitidas (#17), as mesmas de StatusPedido.podeIrPara no backend. */
export const TRANSICOES_STATUS_PEDIDO: Record<number, readonly number[]> = {
  [STATUS_PEDIDO.PENDENTE]: [STATUS_PEDIDO.PAGO, STATUS_PEDIDO.CANCELADO],
  [STATUS_PEDIDO.PAGO]: [STATUS_PEDIDO.SEPARACAO, STATUS_PEDIDO.CANCELADO],
  [STATUS_PEDIDO.SEPARACAO]: [STATUS_PEDIDO.ENVIADO, STATUS_PEDIDO.CANCELADO],
  [STATUS_PEDIDO.ENVIADO]: [STATUS_PEDIDO.ENTREGUE],
  [STATUS_PEDIDO.ENTREGUE]: [STATUS_PEDIDO.EM_DEVOLUCAO],
  [STATUS_PEDIDO.EM_DEVOLUCAO]: [STATUS_PEDIDO.DEVOLVIDO, STATUS_PEDIDO.ENTREGUE],
};

export function podeMudarStatus(atual: number, novo: number): boolean {
  return TRANSICOES_STATUS_PEDIDO[atual]?.includes(novo) ?? false;
}

/** Ids de OrigemRegistro (D11). */
export const ORIGEM_REGISTRO = { MANUAL: 1, SISTEMA: 2, INTEGRACAO: 3 } as const;

export interface ClientePedido {
  id: number;
  nome: string;
}

/** Item de GET /pedidos. quantidadeItens = linhas do pedido. */
export interface PedidoListagem {
  id: number;
  numero: string;
  status: StatusRef;
  cliente: ClientePedido;
  valorTotal: number;
  quantidadeItens: number;
  dataCadastro: string;
}

export interface ItemPedido {
  id: number;
  produto: { id: number; sku: string; nome: string };
  quantidade: number;
  precoUnitarioCongelado: number;
  subtotal: number;
}

/** Endereço congelado no pedido (D12). */
export interface EnderecoPedido {
  destinatario: string;
  logradouro: string;
  numero: string;
  complemento: string | null;
  bairro: string;
  cidade: string;
  uf: string;
  cep: string;
}

/** GET /pedidos/{id}. pagamentos e entrega chegam vazios até as issues de pagamento e entrega. */
export interface Pedido {
  id: number;
  numero: string;
  status: StatusRef;
  cliente: ClientePedido;
  valorTotal: number;
  valorFrete: number;
  valorDesconto: number;
  itens: ItemPedido[];
  enderecoEntrega: EnderecoPedido | null;
  pagamentos: unknown[];
  entrega: unknown | null;
  dataCadastro?: string;
}

/** Item de GET /pedidos/{id}/historico, em ordem cronológica. autor só quando a origem é MANUAL. */
export interface HistoricoStatusPedido {
  statusAnterior: StatusRef | null;
  statusNovo: StatusRef;
  origem: StatusRef;
  autor: { id: number; nome: string } | null;
  observacao: string | null;
  dataCadastro: string;
}

export interface ResumoStatusPedido {
  status: StatusRef;
  quantidade: number;
}

/** Filtros de GET /pedidos; datas AAAA-MM-DD, inclusivas. */
export interface FiltroPedidos {
  idStatus?: number | null;
  dataInicio?: string | null;
  dataFim?: string | null;
  busca?: string | null;
}
