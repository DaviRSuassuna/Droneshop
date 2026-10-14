/** Enum de status como a API devolve: objeto com id e nome (padrão Regiao). */
export interface StatusRef {
  id: number;
  nome: string;
}

/** Ids dos status de marca, categoria e subcategoria. */
export const STATUS_PUBLICACAO = { RASCUNHO: 1, ATIVA: 2, INATIVA: 3 } as const;

/** Ids dos status de produto. */
export const STATUS_PRODUTO = { RASCUNHO: 1, ATIVO: 2, INATIVO: 3 } as const;

/** Ids dos status de fornecedor. */
export const STATUS_FORNECEDOR = { ATIVO: 1, INATIVO: 2 } as const;
