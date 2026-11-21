// Rotas públicas da vitrine (#21, docs/issues/021.md).

export interface TipoLoja {
  id: number;
  nome: 'DRONE' | 'ACESSORIO' | 'KIT' | string;
}

export interface ReferenciaLoja {
  id: number;
  nome: string;
  slug?: string;
}

export interface CategoriaLoja {
  id: number;
  nome: string;
  slug: string;
  tipo: TipoLoja;
  subcategorias: { id: number; nome: string; slug: string }[];
}

export interface MarcaLoja {
  id: number;
  nome: string;
  slug: string;
  logoUrl: string | null;
}

/** Um item por grupo (D40): o produto mais barato representa o grupo. */
export interface ProdutoLojaItem {
  id: number;
  nome: string;
  slug: string;
  tipo: TipoLoja;
  marca: { nome: string } | null;
  precoMinimo: number | null;
  imagemPrincipal: string | null;
  disponivel: boolean;
}

export interface FiltroProdutosLoja {
  categoria?: string;
  subcategoria?: string;
  marca?: string;
  tipo?: 'DRONE' | 'ACESSORIO';
  precoMin?: number | null;
  precoMax?: number | null;
  autonomiaMin?: number | null;
  alcanceMin?: number | null;
  nome?: string;
  ordem?: 'preco' | '-preco' | 'nome' | 'lancamento' | '';
}

export interface FiltrosLoja {
  marcas: { slug: string; nome: string; quantidade: number }[];
  preco: { min: number | null; max: number | null };
}

export interface VersaoLoja {
  id: number;
  rotulo: string | null;
  slug: string;
  preco: number | null;
  disponivel: number;
}

export interface MidiaLoja {
  id: number;
  tipo: { id: number; nome: string };
  url: string;
  principal: boolean;
  ordem: number;
  altText: string | null;
}

export interface ProdutoLojaDetalhe {
  id: number;
  nome: string;
  slug: string;
  tipo: TipoLoja;
  descricaoCurta?: string | null;
  descricao: string | null;
  marca: ReferenciaLoja | null;
  subcategoria: ReferenciaLoja | null;
  garantiaMeses: number;
  metaTitle?: string | null;
  metaDescription?: string | null;
  pesoGramas?: number | null;
  dimensoesDobrado?: string | null;
  autonomiaMinutos?: number | null;
  alcanceMetros?: number | null;
  velocidadeMaximaKmh?: number | null;
  resolucaoFotoMp?: number | null;
  tipoSensor?: string | null;
  resolucaoVideo?: string | null;
  estabilizacao?: string | null;
  tipoBateria?: string | null;
  capacidadeBateriaMah?: number | null;
  tempoCarregamentoMinutos?: number | null;
  gpsIntegrado?: boolean | null;
  sensoresAnticolisao?: boolean | null;
  tipoAcessorio?: string | null;
  sku: string | null;
  preco: number | null;
  disponivel: number;
  imagemPrincipal?: string | null;
  grupo: { nome: string; slug: string; versoes: VersaoLoja[] } | null;
  midias: MidiaLoja[];
}

export interface ProdutoLojaResumo {
  id: number;
  nome: string;
  slug: string;
  precoMinimo: number | null;
}

export interface OfertaLoja {
  produto: ProdutoLojaResumo;
  precoDe: number;
  precoPor: number;
  promocao: { id: number; nome: string; dataFim: string } | null;
}

export interface ConfiguracaoLojaPublica {
  prazoArrependimentoDias: number;
  freteReversoPorContaLoja: boolean;
}
