import { Fornecedor } from './fornecedor.model';
import { Marca } from './marca.model';
import { StatusRef } from './status.model';
import { Subcategoria } from './subcategoria.model';

/** Ids de TipoProduto (GET /tipos-produto). */
export const TIPO_PRODUTO = { DRONE: 1, ACESSORIO: 2 } as const;

/**
 * Variante da resposta (D26): o preço é dela; a margem vem calculada do backend.
 * No kit (D7) não há estoque físico, disponivel vem dos componentes, e peso/dimensões
 * já chegam derivados deles, salvo quando pesoSobrescrito/dimensoesSobrescritas (D16).
 */
export interface Variante {
  id: number;
  nome: string | null;
  sku: string | null;
  preco: number | null;
  custoAquisicao: number | null;
  margemPercentual: number | null;
  estoqueFisico: number | null;
  disponivel: number;
  estoqueMinimo: number;
  pesoEmbalagemKg: number | null;
  alturaEmbalagemCm: number | null;
  larguraEmbalagemCm: number | null;
  comprimentoEmbalagemCm: number | null;
  pesoSobrescrito: boolean;
  dimensoesSobrescritas: boolean;
  ehKit: boolean;
  ativa: boolean;
  componentes: ComponenteKit[];
}

/** Item de GET /variantes: usado nos selects de variante (kit, estoque, promoção). */
export interface VarianteListagem {
  id: number;
  sku: string | null;
  nome: string | null;
  preco: number | null;
  estoqueFisico: number | null;
  disponivel: number;
  ehKit: boolean;
  produto: { id: number; nome: string | null; tipo: StatusRef };
}

/** Filtros aceitos por GET /variantes. Campos vazios são ignorados. */
export interface FiltroVariante {
  busca?: string;
  ativa?: boolean | null;
}

/** Item do kit na resposta: a variante componente e quantas o kit leva. */
export interface ComponenteKit {
  variante: VarianteListagem;
  quantidade: number;
}

/** Item do kit no corpo da variante. */
export interface ComponenteKitDTO {
  idVariante: number | null;
  quantidade: number | null;
}

/**
 * Linha da tabela de variantes no corpo do drone/acessório (D13). Com id altera,
 * sem id cria; a que sai da lista é excluída. estoqueFisico só vale na criação.
 * Kit: sem estoqueFisico; peso e dimensões vazios são derivados dos componentes.
 */
export interface VarianteDTO {
  id: number | null;
  nome: string;
  sku: string;
  preco: number | null;
  custoAquisicao: number | null;
  estoqueFisico: number | null;
  estoqueMinimo: number | null;
  pesoEmbalagemKg: number | null;
  alturaEmbalagemCm: number | null;
  larguraEmbalagemCm: number | null;
  comprimentoEmbalagemCm: number | null;
  ehKit: boolean;
  componentes: ComponenteKitDTO[];
  ativa: boolean;
}

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
  marca: Marca | null;
  subcategoria: Subcategoria | null;
  fornecedor: Fornecedor | null;
  status: StatusRef;
  destaque: boolean;
  variantes: Variante[];
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
  variantes: VarianteDTO[];
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
