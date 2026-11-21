/** Produto como aparece na lista de desejos e no carrinho. */
export interface ProdutoResumo {
  id: number;
  nome: string;
  sku: string;
  slug: string;
  preco: number;
  disponivel: boolean;
}

/** GET /conta/desejos (#22); precoAoAdicionar e caiuPreco são da #86. */
export interface ItemListaDesejos {
  produto: ProdutoResumo;
  adicionadoEm: string;
  precoAoAdicionar: number | null;
  caiuPreco: boolean;
}
