/** Resposta de GET /loja/comparar (#44). */
export interface ComparacaoProduto {
  id: number;
  nome: string;
  slug: string;
  preco: number | null;
  nota: number | null;
  totalAvaliacoes: number;
  doGrupo: boolean;
  itensExtras: string[];
}

export interface ComparacaoSpec {
  campo: string;
  rotulo: string;
  valores: (string | number | boolean | null)[];
  diferente: boolean;
  melhor: number | null;
}

export interface Comparacao {
  produtos: ComparacaoProduto[];
  specs: ComparacaoSpec[];
}
