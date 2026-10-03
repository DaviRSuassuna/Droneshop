import { StatusCategoria } from './categoria.model';

/** Espelha o SubcategoriaResponseDTO devolvido pela API. */
export interface Subcategoria {
  id: number;
  nome: string;
  slug: string;
  categoriaId: number;
  categoriaNome: string;
  ordemExibicao: number;
  descricao: string | null;
  status: StatusCategoria;
}

/** Espelha o SubcategoriaDTO aceito pelo POST /subcategorias e PUT /subcategorias/{id}. */
export interface SubcategoriaDTO {
  nome: string;
  slug: string;
  categoriaId: number | null;
  ordemExibicao: number;
  descricao: string;
  status: StatusCategoria;
}
