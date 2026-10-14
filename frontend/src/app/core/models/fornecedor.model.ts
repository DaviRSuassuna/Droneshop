import { Endereco, EnderecoDTO } from './endereco.model';
import { StatusRef } from './status.model';

/** Espelha o FornecedorResponseDTO devolvido pela API (CNPJ, telefone e CEP só com dígitos). */
export interface Fornecedor {
  id: number;
  razaoSocial: string;
  cnpj: string;
  nomeContato: string;
  emailContato: string;
  telefoneContato: string | null;
  endereco: Endereco | null;
  status: StatusRef;
}

/** Espelha o FornecedorRequestDTO aceito pelo POST /fornecedores e PUT /fornecedores/{id}. */
export interface FornecedorDTO {
  razaoSocial: string;
  cnpj: string;
  nomeContato: string;
  emailContato: string;
  telefoneContato: string;
  endereco: EnderecoDTO;
  idStatus: number;
}
