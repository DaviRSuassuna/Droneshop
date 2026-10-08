/** Espelha o EstadoResponseDTO (dados do IBGE, somente leitura). */
export interface Estado {
  id: number;
  nome: string;
  sigla: string;
  codigoIbge: number;
}

/** Espelha o MunicipioResponseDTO, com o estado aninhado. */
export interface Municipio {
  id: number;
  nome: string;
  codigoIbge: number;
  estado: Estado;
}

/** Espelha o EnderecoResponseDTO (CEP só com dígitos). */
export interface Endereco {
  cep: string;
  logradouro: string;
  numero: string;
  complemento: string | null;
  bairro: string;
  municipio: Municipio;
}

/** Espelha o EnderecoDTO enviado dentro do FornecedorDTO. */
export interface EnderecoDTO {
  cep: string;
  logradouro: string;
  numero: string;
  complemento: string;
  bairro: string;
  idMunicipio: number | null;
}

/** Resposta do GET /ceps/{cep}. municipio vem null se o ViaCEP devolver uma cidade sem correspondência. */
export interface CepResultado {
  cep: string;
  logradouro: string | null;
  complemento: string | null;
  bairro: string | null;
  localidade: string | null;
  uf: string | null;
  municipio: Municipio | null;
}
