import { Endereco, EnderecoDTO } from './endereco.model';

export type StatusFornecedor = 'ATIVO' | 'INATIVO';

/** Espelha o FornecedorResponseDTO devolvido pela API (CNPJ, telefone e CEP só com dígitos). */
export interface Fornecedor {
  id: number;
  razaoSocial: string;
  cnpj: string;
  nomeContato: string;
  emailContato: string;
  telefoneContato: string | null;
  endereco: Endereco | null;
  status: StatusFornecedor;
}

/** Espelha o FornecedorDTO aceito pelo POST /fornecedores e PUT /fornecedores/{id}. */
export interface FornecedorDTO {
  razaoSocial: string;
  cnpj: string;
  nomeContato: string;
  emailContato: string;
  telefoneContato: string;
  endereco: EnderecoDTO;
  status: StatusFornecedor;
}
