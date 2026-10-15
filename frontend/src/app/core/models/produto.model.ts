import { Fornecedor } from './fornecedor.model';
import { Marca } from './marca.model';
import { StatusRef } from './status.model';
import { Subcategoria } from './subcategoria.model';

/**
 * Espelha o ProdutoResponseDTO devolvido pela API (relacionamentos aninhados).
 * Rascunho salva só com o status (D28), por isso quase tudo pode vir null.
 */
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
  /** ISO yyyy-MM-dd */
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
  /** vazio: a API gera a partir do nome */
  slug: string;
  metaTitle: string;
  metaDescription: string;
  tags: string;
  /** null: a API usa o padrão da configuração da loja */
  garantiaMeses: number | null;
  /** ISO yyyy-MM-dd ou null */
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
