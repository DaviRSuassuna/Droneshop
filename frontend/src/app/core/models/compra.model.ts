/** Produto como aparece na lista de desejos e no carrinho. */
export interface ProdutoResumo {
  id: number;
  nome: string;
  sku: string;
  slug: string;
  preco: number;
  disponivel: boolean;
}

/** GET /conta/desejos (#22); precoAoAdicionar e quedaDePreco são da #86. */
export interface ItemListaDesejos {
  produto: ProdutoResumo;
  adicionadoEm: string;
  precoAoAdicionar: number | null;
  quedaDePreco: boolean;
}

/** Carrinho (#24): anônimo pelo header X-Carrinho-Token, do cliente depois do vínculo. */
export interface Carrinho {
  token: string;
  status: { id: number; nome: string };
  itens: ItemCarrinho[];
  subtotal: number;
  desconto: number;
  total: number;
  cupom: { codigo: string; desconto: number } | null;
  avisos: string[];
}

export interface ItemCarrinho {
  id: number;
  produto: { id: number; sku: string; nome: string; slug: string };
  quantidade: number;
  precoUnitario: number;
  subtotal: number;
  disponivel: boolean;
  reservas: {
    id: number;
    produto: { id: number };
    quantidade: number;
    status: { id: number; nome: string };
    expiraEm: string;
  }[];
}

/** Ids de ModalidadeEntrega (#30). */
export const MODALIDADE_ENTREGA = { EXPRESSA: 1, PADRAO: 2, RETIRADA_SHOWROOM: 3 } as const;

/** POST /frete/cotacao (#30). */
export interface CotacaoFrete {
  modalidade: { id: number; nome: string };
  origemCalculo: { id: number; nome: string };
  transportadora: string | null;
  codigoServicoTransportadora: string | null;
  custo: number;
  prazoDias: number;
}

/** Ids de MetodoPagamento (#27). */
export const METODO_PAGAMENTO = { PIX: 1, CARTAO: 2, BOLETO: 3, FATURAMENTO_PJ: 4 } as const;

/** POST /checkout (#26). */
export interface CheckoutDTO {
  idEndereco: number;
  idModalidadeEntrega: number;
  codigoServicoTransportadora: string | null;
  idMetodoPagamento: number;
}

/** POST /conta/desejos/mover-para-carrinho (#86). */
export interface MoverParaCarrinho {
  carrinho: Carrinho;
  naoAdicionados: { idProduto: number; motivo: string }[];
}

/** GET /loja/listas/{token} (#86). */
export interface ListaCompartilhada {
  nomeCliente: string;
  itens: { produto: ProdutoResumo }[];
}

/** GET /conta/cartoes (#28): nunca o token do gateway. */
export interface CartaoSalvo {
  id: number;
  ultimosDigitos: string;
  bandeira: string | null;
  validade: string;
  padrao: boolean;
}

/** POST /pedidos/{id}/pagamentos (#27). */
export interface PagamentoDTO {
  idMetodo: number;
  tokenCartao?: string | null;
  idCartaoSalvo?: number | null;
  salvarCartao?: boolean;
}

/** GET /conta/drones (#88). */
export interface DroneDaConta {
  idUnidade: number;
  produto: { id: number; nome: string; slug: string };
  numeroSerie: string;
  registroAnac: string | null;
  dataCompra: string;
  validadeGarantia: string | null;
  pedido: { id: number; numero: string };
}

/** Devolução (#37). */
export interface Devolucao {
  id: number;
  pedido: { id: number; numero: string };
  status: { id: number; nome: string };
  resolucao: { id: number; nome: string };
  motivo: string;
  dataSolicitacao: string;
  dataConclusao: string | null;
  itens: { id: number; idItemPedido: number; produto: string; quantidade: number }[];
}

export const RESOLUCAO_DEVOLUCAO = { REEMBOLSO: 1, TROCA: 2, REPARO: 3 } as const;

export interface DevolucaoDTO {
  idPedido: number;
  idResolucao: number;
  motivo: string;
  itens: { idItemPedido: number; quantidade: number }[];
}
