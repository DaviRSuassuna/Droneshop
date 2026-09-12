export type StatusCategoria = 'RASCUNHO' | 'ATIVA' | 'INATIVA';

export interface Categoria {
  id: number;
  nome: string;
  slug: string;
  categoriaPaiId: number | null;
  categoriaPaiNome: string | null;
  ordemExibicao: number;
  descricao: string | null;
  status: StatusCategoria;
  exibirMenu: boolean;
  iconeUrl: string | null;
}

export interface CategoriaDTO {
  nome: string;
  slug: string;
  categoriaPaiId: number | null;
  ordemExibicao: number;
  descricao: string;
  status: StatusCategoria;
  exibirMenu: boolean;
  iconeUrl: string;
}
