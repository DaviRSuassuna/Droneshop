import { Fornecedor } from './fornecedor.model';
import { Marca } from './marca.model';
import { StatusRef } from './status.model';
import { Subcategoria } from './subcategoria.model';

/** Ids de TipoProduto (GET /tipos-produto). */
export const TIPO_PRODUTO = { DRONE: 1, ACESSORIO: 2 } as const;

/** Campos comuns da resposta de /produtos (relacionamentos aninhados). */
interface ProdutoBase {
  id: number;
  nome: string | null;
  modelo: string | null;
  descricaoCurta: string | null;
  descricao: string | null;
  slug: string;
  metaTitle: string | null;
  metaDescription: string | null;
  tags: string | null;
  garantiaMeses: number;
  dataLancamento: string | null;
  preco: number | null;
  marca: Marca | null;
  subcategoria: Subcategoria | null;
  fornecedor: Fornecedor | null;
  status: StatusRef;
  destaque: boolean;
}

/** Specs do drone, colunas fixas (D14). */
export interface DroneSpecs {
  pesoGramas: number | null;
  dimensoesDobrado: string | null;
  dimensoesAberto: string | null;
  autonomiaMinutos: number | null;
  alcanceMetros: number | null;
  velocidadeMaximaKmh: number | null;
  resolucaoFotoMp: number | null;
  tipoSensor: string | null;
  resolucaoVideo: string | null;
  estabilizacao: string | null;
  tipoBateria: string | null;
  capacidadeBateriaMah: number | null;
  tempoCarregamentoMinutos: number | null;
  gpsIntegrado: boolean;
  sensoresAnticolisao: boolean;
  exigeRegistroAnac: boolean;
  classeAnac: string | null;
  registroSisant: string | null;
}

/** Produto citado dentro de outro, como na compatibilidade (D17). */
export interface ProdutoResumo {
  id: number;
  nome: string | null;
  tipo: StatusRef;
}

/**
 * Item de GET /produtos e resposta de GET /produtos/{id}. O tipo diz quais campos
 * vêm: drone traz as specs, acessório traz tipoAcessorio.
 */
export interface Produto extends ProdutoBase, Partial<DroneSpecs> {
  tipo: StatusRef;
  tipoAcessorio?: string | null;
  /** Só no acessório: produtos com que ele é compatível (D17). */
  compativeis?: ProdutoResumo[];
  /** Em todo produto: acessórios que se declararam compatíveis com ele (lado recíproco). */
  acessoriosCompativeis?: ProdutoResumo[];
}

/** Campos comuns do corpo de POST/PUT em /drones e /acessorios. */
export interface ProdutoDTO {
  nome: string;
  modelo: string;
  descricaoCurta: string;
  descricao: string;
  slug: string;
  metaTitle: string;
  metaDescription: string;
  tags: string;
  garantiaMeses: number | null;
  dataLancamento: string | null;
  idMarca: number | null;
  idSubcategoria: number | null;
  idFornecedor: number | null;
  idStatus: number;
  destaque: boolean;
}

/** Corpo de POST /drones e PUT /drones/{id}. */
export type DroneDTO = ProdutoDTO & DroneSpecs;

/** Corpo de POST /acessorios e PUT /acessorios/{id}. */
export interface AcessorioDTO extends ProdutoDTO {
  tipoAcessorio: string;
  idsCompativeis: number[];
}

/** Filtros aceitos por GET /produtos. Campos vazios são ignorados. */
export interface FiltroProduto {
  idTipo?: number | null;
  nome?: string;
  idMarca?: number | null;
  idCategoria?: number | null;
  idSubcategoria?: number | null;
  idStatus?: number | null;
}
