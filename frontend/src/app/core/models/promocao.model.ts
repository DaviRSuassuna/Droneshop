import { PageResponse } from './page.model';

/** Escopos disponibilizados por GET /escopos-promocao. */
export const ESCOPO_PROMOCAO = {
  PRODUTO: 1,
  CARRINHO: 2,
  FRETE: 3,
} as const;

export interface EscopoPromocaoRef {
  id: number;
  nome: 'PRODUTO' | 'CARRINHO' | 'FRETE' | string;
}

/** Variante associada a uma promoção na resposta de /promocoes. */
export interface VariantePromocao {
  id: number;
  sku: string | null;
  nome: string | null;
}

/** Resposta de GET /promocoes e GET /promocoes/{id}. */
export interface Promocao {
  id: number;
  nome: string;
  escopo: EscopoPromocaoRef;
  dataInicio: string;
  dataFim: string;
  percentualDesconto: number | null;
  valorDesconto: number | null;
  valorMinimoCarrinho: number | null;
  ativa: boolean;
  situacao: 'VIGENTE' | 'AGENDADA' | 'ENCERRADA' | 'INATIVA' | string;
  variantes: VariantePromocao[];
}

/** Corpo de POST /promocoes e PUT /promocoes/{id}. */
export interface PromocaoDTO {
  nome: string;
  idEscopo: number;
  dataInicio: string;
  dataFim: string;
  percentualDesconto: number | null;
  valorDesconto: number | null;
  valorMinimoCarrinho: number | null;
  ativa: boolean;
  idsVariantes: number[];
}
