import { Fornecedor } from './fornecedor.model';
import { Marca } from './marca.model';
import { StatusRef } from './status.model';
import { Subcategoria } from './subcategoria.model';

/** Ids de TipoProduto (GET /tipos-produto). KIT é o terceiro tipo (D40). */
export const TIPO_PRODUTO = { DRONE: 1, ACESSORIO: 2, KIT: 3 } as const;

/** Grupo citado no produto (D40). */
export interface GrupoResumo {
  id: number;
  nome: string;
}

/** Item do kit na resposta: o produto componente e quantas unidades o kit leva (D7). */
export interface ComponenteKit {
  idProduto: number;
  sku: string | null;
  nome: string | null;
  tipo: StatusRef;
  status: StatusRef;
  estoqueFisico: number | null;
  disponivel: number;
  quantidade: number;
}

/** Item do kit no corpo de /kits: qualquer produto que não seja outro kit. */
export interface ComponenteKitDTO {
  idProduto: number | null;
  quantidade: number | null;
}

/**
 * Campos comuns da resposta de /produtos (relacionamentos aninhados). Cada versão é um
 * produto (D40): SKU, preço, custo, estoque e embalagem são dele; a margem vem
 * calculada do backend (D26). No kit, estoqueFisico é nulo e disponivel vem dos componentes.
 */
interface ProdutoBase {
  id: number;
  nome: string | null;
  sku: string | null;
  descricaoCurta: string | null;
  descricao: string | null;
  slug: string;
  metaTitle: string | null;
  metaDescription: string | null;
  tags: string | null;
  garantiaMeses: number;
  dataLancamento: string | null;
  marca: Marca | null;
  subcategoria: Subcategoria | null;
  fornecedor: Fornecedor | null;
  status: StatusRef;
  destaque: boolean;
  preco: number | null;
  custoAquisicao: number | null;
  margemPercentual: number | null;
  estoqueFisico: number | null;
  disponivel: number;
  pesoEmbalagemKg: number | null;
  alturaEmbalagemCm: number | null;
  larguraEmbalagemCm: number | null;
  comprimentoEmbalagemCm: number | null;
  grupo: GrupoResumo | null;
  rotuloNoGrupo: string | null;
  ordemNoGrupo: number;
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
 * vêm: drone traz as specs, acessório traz tipoAcessorio, kit traz os componentes.
 */
export interface Produto extends ProdutoBase, Partial<DroneSpecs> {
  tipo: StatusRef;
  /** Drone e acessório; o kit não tem (o mínimo é do componente). */
  estoqueMinimo?: number;
  tipoAcessorio?: string | null;
  /** Só no acessório: produtos com que ele é compatível (D17). */
  compativeis?: ProdutoResumo[];
  /** Em todo produto: acessórios que se declararam compatíveis com ele (lado recíproco). */
  acessoriosCompativeis?: ProdutoResumo[];
  /** Só no kit. */
  componentes?: ComponenteKit[];
  /** Só no kit: peso ou dimensões informados no próprio kit, em vez dos derivados (D16). */
  pesoSobrescrito?: boolean;
  dimensoesSobrescritas?: boolean;
}

/**
 * Campos comuns do corpo de POST/PUT em /drones, /acessorios e /kits. Preço, estoque e
 * embalagem vão no próprio produto (D40); estoqueFisico só vale na criação.
 */
export interface ProdutoDTO {
  nome: string;
  sku: string;
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
  preco: number | null;
  custoAquisicao: number | null;
  pesoEmbalagemKg: number | null;
  alturaEmbalagemCm: number | null;
  larguraEmbalagemCm: number | null;
  comprimentoEmbalagemCm: number | null;
  idGrupo: number | null;
  rotuloNoGrupo: string;
  ordemNoGrupo: number | null;
}

/** Estoque de drone e acessório: estoqueFisico só na criação (saldo inicial). */
export interface EstoqueDTO {
  estoqueFisico: number | null;
  estoqueMinimo: number | null;
}

/** Corpo de POST /drones e PUT /drones/{id}. */
export type DroneDTO = ProdutoDTO & EstoqueDTO & DroneSpecs;

/** Corpo de POST /acessorios e PUT /acessorios/{id}. */
export interface AcessorioDTO extends ProdutoDTO, EstoqueDTO {
  tipoAcessorio: string;
  idsCompativeis: number[];
}

/** Corpo de POST /kits e PUT /kits/{id}: sem estoque próprio; peso e dimensões vazios vêm dos componentes. */
export interface KitDTO extends ProdutoDTO {
  componentes: ComponenteKitDTO[];
}

/**
 * Filtros aceitos por GET /produtos. Campos vazios são ignorados. busca procura no nome
 * e no SKU; idStatus filtra pelo status do produto e kit=false tira os kits (select de produto, D40).
 */
export interface FiltroProduto {
  idTipo?: number | null;
  busca?: string;
  idMarca?: number | null;
  idCategoria?: number | null;
  idSubcategoria?: number | null;
  idStatus?: number | null;
  idGrupo?: number | null;
  kit?: boolean | null;
  /** Mesma regra do GET /estoque?situacao= (kit fica de fora). */
  estoque?: 'ABAIXO_MINIMO' | 'ZERADO' | null;
}

export interface ProdutoLoteRequest {
  ids: number[];
  acao: 'ATIVAR' | 'INATIVAR' | 'MOVER_SUBCATEGORIA' | 'REAJUSTAR_PRECO';
  idSubcategoria?: number;
  percentual?: number;
}
export interface ProdutoLoteResponse {
  processados: number;
  falhas: { id: number; motivo: string }[];
}
