export type StatusMarca = 'RASCUNHO' | 'ATIVA' | 'INATIVA';

/** Espelha o retorno da API (entidade Marca serializada pelo backend). */
export interface Marca {
  id: number;
  nome: string;
  slug: string;
  paisOrigem: string;
  siteOficial: string | null;
  descricao: string | null;
  logoUrl: string;
  status: StatusMarca;
  destacarHome: boolean;
  dataCadastro?: string;
  dataAlteracao?: string;
}

/** Espelha o MarcaDTO aceito pelo POST /marcas e PUT /marcas/{id}. */
export interface MarcaDTO {
  nome: string;
  slug: string;
  paisOrigem: string;
  siteOficial: string;
  descricao: string;
  logoUrl: string;
  status: StatusMarca;
  destacarHome: boolean;
}
