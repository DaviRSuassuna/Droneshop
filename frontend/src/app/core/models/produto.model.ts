import { Fornecedor } from './fornecedor.model';
import { Marca } from './marca.model';
import { StatusRef } from './status.model';
import { Subcategoria } from './subcategoria.model';

/** Espelha o ProdutoResponseDTO devolvido pela API (relacionamentos aninhados). */
export interface Produto {
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

/** Espelha o ProdutoRequestDTO aceito pelo POST /produtos e PUT /produtos/{id}. */
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
  preco: number | null;
  idMarca: number | null;
  idSubcategoria: number | null;
  idFornecedor: number | null;
  idStatus: number;
  destaque: boolean;
}

/** Filtros aceitos por GET /produtos. Campos vazios são ignorados. */
export interface FiltroProduto {
  nome?: string;
  idMarca?: number | null;
  idCategoria?: number | null;
  idSubcategoria?: number | null;
  idStatus?: number | null;
}
