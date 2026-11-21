import { StatusRef } from './status.model';

/** Espelha o MarcaResponseDTO devolvido pela API. */
export interface Marca {
  quantidadeProdutos: number;
  id: number;
  nome: string;
  slug: string;
  paisOrigem: string;
  siteOficial: string | null;
  descricao: string | null;
  logoUrl: string;
  status: StatusRef;
  destacarHome: boolean;
}

/** Resposta do POST /marcas/{id}/logo (#84). */
export interface MarcaLogo {
  logoUrl: string;
}

/** Espelha o MarcaRequestDTO aceito pelo POST /marcas e PUT /marcas/{id}. */
export interface MarcaDTO {
  nome: string;
  slug: string;
  paisOrigem: string;
  siteOficial: string;
  descricao: string;
  logoUrl: string;
  idStatus: number;
  destacarHome: boolean;
}
