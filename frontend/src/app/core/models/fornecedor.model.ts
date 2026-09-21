export type StatusFornecedor = 'ATIVO' | 'INATIVO';

/** Espelha o FornecedorResponseDTO devolvido pela API. */
export interface Fornecedor {
  id: number;
  razaoSocial: string;
  cnpj: string;
  nomeContato: string;
  emailContato: string;
  telefoneContato: string | null;
  status: StatusFornecedor;
}

/** Espelha o FornecedorDTO aceito pelo POST /fornecedores e PUT /fornecedores/{id}. */
export interface FornecedorDTO {
  razaoSocial: string;
  cnpj: string;
  nomeContato: string;
  emailContato: string;
  telefoneContato: string;
  status: StatusFornecedor;
}
