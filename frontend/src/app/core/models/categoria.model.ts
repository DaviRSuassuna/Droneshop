import { StatusRef } from './status.model';

/** Espelha o CategoriaResponseDTO devolvido pela API. */
export interface Categoria {
  id: number;
  nome: string;
  slug: string;
  ordemExibicao: number;
  descricao: string | null;
  status: StatusRef;
  exibirMenu: boolean;
  iconeUrl: string | null;
}

/** Espelha o CategoriaRequestDTO aceito pelo POST /categorias e PUT /categorias/{id}. */
export interface CategoriaDTO {
  nome: string;
  slug: string;
  ordemExibicao: number;
  descricao: string;
  idStatus: number;
  exibirMenu: boolean;
  iconeUrl: string;
}
