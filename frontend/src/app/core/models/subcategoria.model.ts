import { Categoria } from './categoria.model';
import { StatusRef } from './status.model';

/** Espelha o SubcategoriaResponseDTO devolvido pela API (categoria aninhada). */
export interface Subcategoria {
  quantidadeProdutos: number;
  id: number;
  nome: string;
  slug: string;
  categoria: Categoria | null;
  ordemExibicao: number;
  descricao: string | null;
  status: StatusRef;
}

/** Espelha o SubcategoriaRequestDTO aceito pelo POST /subcategorias e PUT /subcategorias/{id}. */
export interface SubcategoriaDTO {
  nome: string;
  slug: string;
  idCategoria: number | null;
  ordemExibicao: number;
  descricao: string;
  idStatus: number;
}
