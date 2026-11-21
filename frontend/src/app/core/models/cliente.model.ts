import { StatusRef } from './status.model';
import { Municipio } from './endereco.model';

/** Ids do enum TipoPessoa (GET /tipos-pessoa). */
export const TIPO_PESSOA = { PF: 1, PJ: 2 } as const;

/** Corpo do POST /clientes (criar conta). Documento e telefone podem ir com máscara. */
export interface ContaDTO {
  nome: string;
  email: string;
  senha: string;
  idTipoPessoa: number;
  documento: string;
  telefone: string;
}

/** Resposta do POST /clientes; id é o do usuário (o mesmo de /auth/me). */
export interface Cliente {
  id: number;
  nome: string;
  email: string;
  tipoPessoa: StatusRef;
  documento: string;
  telefone: string | null;
}

/** GET /conta/perfil. */
export interface PerfilCliente {
  nome: string;
  email: string;
  tipoPessoa: StatusRef;
  documento: string;
  telefone: string | null;
}

/** PUT /conta/perfil: documento e tipo de pessoa não mudam. */
export interface PerfilDTO {
  nome: string;
  telefone: string;
}

/** PUT /conta/senha. */
export interface SenhaDTO {
  senhaAtual: string;
  senhaNova: string;
}

/** GET /conta/enderecos. */
export interface EnderecoCliente {
  id: number;
  apelido: string | null;
  cep: string;
  logradouro: string;
  numero: string;
  complemento: string | null;
  bairro: string;
  municipio: Municipio;
  principal: boolean;
}

/** POST e PUT /conta/enderecos. */
export interface EnderecoClienteDTO {
  apelido: string;
  cep: string;
  logradouro: string;
  numero: string;
  complemento: string;
  bairro: string;
  idMunicipio: number | null;
  principal: boolean;
}
