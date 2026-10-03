export type StatusProduto = 'RASCUNHO' | 'ATIVO' | 'INATIVO';

/** Espelha o ProdutoResponseDTO devolvido pela API (marca, subcategoria e categoria achatadas). */
export interface Produto {
  id: number;
  nome: string;
  modelo: string;
  descricaoCurta: string | null;
  preco: number;
  marcaId: number;
  marcaNome: string;
  subcategoriaId: number;
  subcategoriaNome: string;
  categoriaId: number;
  categoriaNome: string;
  fornecedorId: number;
  fornecedorRazaoSocial: string;
  status: StatusProduto;
  destaque: boolean;
}

/** Espelha o ProdutoDTO aceito pelo POST /produtos e PUT /produtos/{id}. */
export interface ProdutoDTO {
  nome: string;
  modelo: string;
  descricaoCurta: string;
  preco: number | null;
  marcaId: number | null;
  subcategoriaId: number | null;
  fornecedorId: number | null;
  status: StatusProduto;
  destaque: boolean;
}

/** Filtros aceitos por GET /produtos e GET /produtos/count. Campos vazios são ignorados. */
export interface FiltroProduto {
  nome?: string;
  marcaId?: number | null;
  categoriaId?: number | null;
  subcategoriaId?: number | null;
  status?: StatusProduto | '';
}
