import { StatusRef } from './status.model';

/** Ids de TipoMovimento (D31, ordem do diagrama). */
export const TIPO_MOVIMENTO = {
  ENTRADA: 1,
  SAIDA: 2,
  AJUSTE: 3,
  DEVOLUCAO: 4,
  PERDA: 5,
} as const;

/** Ids de OrigemRegistro (D11). */
export const ORIGEM_REGISTRO = { MANUAL: 1, SISTEMA: 2, INTEGRACAO: 3 } as const;

/** Rótulos do protótipo para cada tipo de movimento. */
export const EXIBICAO_TIPO_MOVIMENTO: Record<number, { rotulo: string; descricao: string }> = {
  [TIPO_MOVIMENTO.ENTRADA]: { rotulo: 'Entrada', descricao: 'compra de fornecedor' },
  [TIPO_MOVIMENTO.SAIDA]: { rotulo: 'Saída', descricao: 'venda ou expedição' },
  [TIPO_MOVIMENTO.AJUSTE]: { rotulo: 'Ajuste', descricao: 'correção de inventário' },
  [TIPO_MOVIMENTO.DEVOLUCAO]: { rotulo: 'Devolução', descricao: 'retorno de cliente' },
  [TIPO_MOVIMENTO.PERDA]: { rotulo: 'Perda', descricao: 'avaria, furto ou vencimento' },
};

/** Filtro de GET /estoque?situacao=. */
export type SituacaoEstoque = 'TODOS' | 'ABAIXO_MINIMO' | 'ZERADO';

/** Item de GET /estoque. No kit, estoqueFisico é nulo e o disponível vem dos componentes (D7). */
export interface ItemEstoque {
  variante: { id: number; sku: string | null; nome: string | null; ehKit: boolean; ativa: boolean };
  produto: { id: number; nome: string | null };
  estoqueFisico: number | null;
  reservado: number;
  disponivel: number;
  estoqueMinimo: number;
  abaixoDoMinimo: boolean;
}

export interface FiltroEstoque {
  situacao?: SituacaoEstoque | null;
  busca?: string | null;
}

/**
 * Item de GET /estoque/movimentos. quantidade é a informada; no AJUSTE, a diferença
 * entre o saldo novo e o anterior. autor só quando a origem é MANUAL (D11).
 */
export interface MovimentoEstoque {
  id: number;
  tipo: StatusRef;
  origem: StatusRef;
  quantidade: number;
  saldoResultante: number;
  observacao: string | null;
  autor: { id: number; nome: string } | null;
  dataCadastro: string;
  variante: { id: number; sku: string | null; nome: string | null };
}

export interface FiltroMovimentos {
  idVariante?: number | null;
  idTipo?: number | null;
}

/** Corpo de POST /estoque/movimentos. No AJUSTE, quantidade é o saldo novo. */
export interface MovimentoEstoqueRequest {
  idVariante: number;
  idTipo: number;
  quantidade: number;
  observacao: string | null;
}
