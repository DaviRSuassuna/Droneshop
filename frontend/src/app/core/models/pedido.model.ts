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
  [STATUS_PEDIDO.PENDENTE]: { rotulo: 'Aguardando pagamento', cor: 'var(--dim2)' },
  [STATUS_PEDIDO.PAGO]: { rotulo: 'Pago', cor: 'var(--ok)' },
  [STATUS_PEDIDO.SEPARACAO]: { rotulo: 'Em separação', cor: 'var(--ok)' },
  [STATUS_PEDIDO.ENVIADO]: { rotulo: 'Enviado', cor: 'var(--ok)' },
  [STATUS_PEDIDO.ENTREGUE]: { rotulo: 'Entregue', cor: 'var(--ok)' },
  [STATUS_PEDIDO.EM_DEVOLUCAO]: { rotulo: 'Em devolução', cor: 'var(--warn)' },
  [STATUS_PEDIDO.DEVOLVIDO]: { rotulo: 'Devolvido', cor: 'var(--dim2)' },
  [STATUS_PEDIDO.CANCELADO]: { rotulo: 'Cancelado', cor: 'var(--warn)' },
};

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
  variante: { id: number; sku: string; nome: string };
  produto: { id: number; nome: string };
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
