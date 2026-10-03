export type StatusCategoria = 'RASCUNHO' | 'ATIVA' | 'INATIVA';

/** Espelha o CategoriaResponseDTO devolvido pela API. */
export interface Categoria {
  id: number;
  nome: string;
  slug: string;
  ordemExibicao: number;
  descricao: string | null;
  status: StatusCategoria;
  exibirMenu: boolean;
  iconeUrl: string | null;
}

/** Espelha o CategoriaDTO aceito pelo POST /categorias e PUT /categorias/{id}. */
export interface CategoriaDTO {
  nome: string;
  slug: string;
  ordemExibicao: number;
  descricao: string;
  status: StatusCategoria;
  exibirMenu: boolean;
  iconeUrl: string;
}
