import { StatusRef } from './status.model';

/** Espelha o CategoriaResponseDTO devolvido pela API. */
export interface Categoria {
  id: number;
  nome: string;
  slug: string;
  ordemExibicao: number;
  descricao: string | null;
  status: StatusRef;
  /** Drone ou acessório: a categoria organiza a vitrine dentro de um tipo. Nulo só no rascunho. */
  tipo: StatusRef | null;
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
  idTipo: number | null;
  exibirMenu: boolean;
  iconeUrl: string;
}
