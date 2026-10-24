import { PageResponse } from './page.model';

export const TIPO_DESCONTO_CUPOM = { PERCENTUAL: 1, VALOR_FIXO: 2 } as const;

export interface TipoDescontoCupomRef {
  id: number;
  nome: 'PERCENTUAL' | 'VALOR_FIXO' | string;
}

export interface ReferenciaCupom {
  id: number;
  nome: string | null;
  sku?: string | null;
}

export interface Cupom {
  id: number;
  codigo: string;
  tipoDesconto: TipoDescontoCupomRef;
  valor: number;
  dataInicio: string;
  dataFim: string | null;
  valorMinimo: number | null;
  limiteTotalUso: number | null;
  limitePorCliente: number | null;
  ativo: boolean;
  situacao: 'VIGENTE' | 'AGENDADO' | 'ENCERRADO' | 'ESGOTADO' | 'INATIVO' | string;
  usos: number;
  categorias: ReferenciaCupom[];
  marcas: ReferenciaCupom[];
  produtos: ReferenciaCupom[];
}

export interface CupomDTO {
  codigo: string;
  idTipoDesconto: number;
  valor: number;
  dataInicio: string;
  dataFim: string | null;
  valorMinimo: number | null;
  limiteTotalUso: number | null;
  limitePorCliente: number | null;
  ativo: boolean;
  idsCategorias: number[];
  idsMarcas: number[];
  idsProdutos: number[];
}

export interface UsoCupom {
  id: number;
  pedido: { id: number };
  cliente: { id: number; nome: string };
  dataUso: string;
}

export type CupomPage = PageResponse<Cupom>;
