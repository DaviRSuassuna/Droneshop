import { Fornecedor } from './fornecedor.model';
import { Marca } from './marca.model';
import { StatusRef } from './status.model';
import { Subcategoria } from './subcategoria.model';

/** Espelha o ProdutoResponseDTO devolvido pela API (relacionamentos aninhados). */
export interface Produto {
  id: number;
  nome: string;
  modelo: string;
  descricaoCurta: string | null;
  preco: number;
  marca: Marca;
  subcategoria: Subcategoria;
  fornecedor: Fornecedor;
  status: StatusRef;
  destaque: boolean;
}

/** Espelha o ProdutoRequestDTO aceito pelo POST /produtos e PUT /produtos/{id}. */
export interface ProdutoDTO {
  nome: string;
  modelo: string;
  descricaoCurta: string;
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
