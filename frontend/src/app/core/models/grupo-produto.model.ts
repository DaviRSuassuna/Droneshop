import { StatusRef } from './status.model';

/** Versão do grupo na resposta, na ordem do seletor "Versão" da loja (D40). */
export interface VersaoGrupo {
  idProduto: number;
  nome: string | null;
  sku: string | null;
  tipo: StatusRef;
  status: StatusRef;
  rotulo: string;
  ordem: number;
  preco: number | null;
  /** URL da imagem principal (#87) ou null sem imagem. */
  imagemPrincipal?: string | null;
}

/**
 * Resposta de GET /grupos-produto. precoAPartirDe é o menor preço entre as versões
 * ATIVAS (nulo se nenhuma está ativa), o "a partir de" do card na vitrine.
 */
export interface GrupoProduto {
  id: number;
  nome: string;
  slug: string;
  descricaoCurta: string | null;
  ativo: boolean;
  precoAPartirDe: number | null;
  versoes: VersaoGrupo[];
}

/** Filtros de GET /grupos-produto. */
export interface FiltroGrupo {
  nome?: string;
  ativo?: boolean | null;
}

/** Corpo de POST /grupos-produto e PUT /grupos-produto/{id}. Slug vazio sai do nome. */
export interface GrupoProdutoDTO {
  nome: string;
  slug: string;
  descricaoCurta: string;
  ativo: boolean;
}
