/**
 * Mapa tela do protótipo -> rota do Angular -> issue.
 *
 * Este mapa é o contrato das rotas do frontend: quem implementa uma tela usa a
 * rota daqui. A etiqueta é a mesma da issue no backend ("issue-21") ou "base"
 * para as telas entregues antes das issues. As telas que já seguem o protótipo
 * ficam em e2e/telas-entregues.txt e bloqueiam o merge no CI.
 *
 * api: respostas simuladas específicas da tela (caminho da API -> corpo JSON),
 *      somadas às respostas padrão de api-falsa.ts.
 * ignorar: textos do protótipo que são dado de exemplo, não estrutura da tela.
 */
export interface Tela {
  prototipo: string;
  rota: string;
  etiqueta: string;
  area: 'admin' | 'loja';
  api?: Record<string, unknown>;
  ignorar?: string[];
}

const pedido = {
  id: 1,
  numero: 'AER-000001',
  status: { id: 2, nome: 'PAGO' },
  cliente: { id: 1, nome: 'Cliente Semente' },
  valorProdutos: 499.0,
  valorFrete: 39.9,
  valorDesconto: 0.0,
  valorTotal: 538.9,
  itens: [
    {
      id: 1,
      variante: { id: 1, sku: 'BAT-5000', nome: 'Única' },
      produto: { id: 1, nome: 'Bateria Inteligente 5000 mAh' },
      quantidade: 1,
      precoUnitarioCongelado: 499.0,
      subtotal: 499.0,
    },
  ],
  enderecoEntrega: {
    destinatario: 'Cliente Semente',
    logradouro: 'Quadra 104 Norte',
    numero: '10',
    complemento: '',
    bairro: 'Plano Diretor Norte',
    cidade: 'Palmas',
    uf: 'TO',
    cep: '77001002',
  },
  descontos: [],
  pagamentos: [],
  entrega: null,
};

const produto = {
  id: 1,
  nome: 'Falcon X2 Pro',
  slug: 'falcon-x2-pro',
  tipo: { id: 1, nome: 'DRONE' },
  descricao: 'Drone de exemplo.',
  marca: { id: 1, nome: 'Aerion' },
  subcategoria: { id: 1, nome: 'Profissionais', categoria: { id: 1, nome: 'Drones' } },
  garantiaMeses: 12,
  autonomiaMinutos: 46,
  alcanceMetros: 15000,
  variantes: [{ id: 1, nome: 'Padrão', sku: 'FX2P-STD', preco: 7999.0, disponivel: 5 }],
  midias: [],
};

export const TELAS: Tela[] = [
  // ------------------------------------------------------------ admin
  { prototipo: 'admin/marcas.html', rota: '/admin/marcas', etiqueta: 'base', area: 'admin' },
  { prototipo: 'admin/marca-nova.html', rota: '/admin/marcas/novo', etiqueta: 'base', area: 'admin' },
  { prototipo: 'admin/categorias.html', rota: '/admin/categorias', etiqueta: 'base', area: 'admin' },
  { prototipo: 'admin/categoria-nova.html', rota: '/admin/categorias/novo', etiqueta: 'base', area: 'admin' },
  { prototipo: 'admin/fornecedores.html', rota: '/admin/fornecedores', etiqueta: 'base', area: 'admin' },
  { prototipo: 'admin/fornecedor-novo.html', rota: '/admin/fornecedores/novo', etiqueta: 'base', area: 'admin' },
  { prototipo: 'admin/produtos.html', rota: '/admin/produtos', etiqueta: 'issue-8', area: 'admin' },
  { prototipo: 'admin/produto-novo.html', rota: '/admin/drones/novo', etiqueta: 'issue-8', area: 'admin' },
  { prototipo: 'admin/acessorio-novo.html', rota: '/admin/acessorios/novo', etiqueta: 'issue-8', area: 'admin' },
  { prototipo: 'admin/usuarios.html', rota: '/admin/usuarios', etiqueta: 'issue-5', area: 'admin' },
  { prototipo: 'admin/usuario-novo.html', rota: '/admin/usuarios/novo', etiqueta: 'issue-5', area: 'admin' },
  { prototipo: 'admin/estoque.html', rota: '/admin/estoque', etiqueta: 'issue-13', area: 'admin' },
  { prototipo: 'admin/estoque-movimento.html', rota: '/admin/estoque/movimento', etiqueta: 'issue-13', area: 'admin' },
  { prototipo: 'admin/promocoes.html', rota: '/admin/promocoes', etiqueta: 'issue-14', area: 'admin' },
  { prototipo: 'admin/promocao-nova.html', rota: '/admin/promocoes/nova', etiqueta: 'issue-14', area: 'admin' },
  { prototipo: 'admin/cupons.html', rota: '/admin/cupons', etiqueta: 'issue-15', area: 'admin' },
  { prototipo: 'admin/cupom-novo.html', rota: '/admin/cupons/novo', etiqueta: 'issue-15', area: 'admin' },
  { prototipo: 'admin/pedidos.html', rota: '/admin/pedidos', etiqueta: 'issue-16', area: 'admin' },
  {
    prototipo: 'admin/pedido.html',
    rota: '/admin/pedidos/1',
    etiqueta: 'issue-16',
    area: 'admin',
    api: { '/pedidos/1': pedido, '/pedidos/1/historico': [] },
  },
  { prototipo: 'admin/index.html', rota: '/admin', etiqueta: 'issue-18', area: 'admin' },

  // ------------------------------------------------------------ loja
  { prototipo: 'index.html', rota: '/', etiqueta: 'issue-21', area: 'loja' },
  { prototipo: 'drones.html', rota: '/drones', etiqueta: 'issue-21', area: 'loja' },
  { prototipo: 'acessorios.html', rota: '/acessorios', etiqueta: 'issue-21', area: 'loja' },
  {
    prototipo: 'produto.html',
    rota: '/produto/falcon-x2-pro',
    etiqueta: 'issue-21',
    area: 'loja',
    api: { '/loja/produtos/falcon-x2-pro': produto, '/loja/produtos/falcon-x2-pro/compativeis': [] },
  },
  { prototipo: 'ofertas.html', rota: '/ofertas', etiqueta: 'issue-21', area: 'loja' },
  { prototipo: 'login.html', rota: '/entrar', etiqueta: 'issue-4', area: 'loja' },
  { prototipo: 'conta-perfil.html', rota: '/conta/perfil', etiqueta: 'issue-19', area: 'loja' },
  { prototipo: 'conta-enderecos.html', rota: '/conta/enderecos', etiqueta: 'issue-19', area: 'loja' },
  { prototipo: 'conta-favoritos.html', rota: '/conta/favoritos', etiqueta: 'issue-22', area: 'loja' },
  { prototipo: 'carrinho.html', rota: '/carrinho', etiqueta: 'issue-24', area: 'loja' },
  { prototipo: 'checkout.html', rota: '/checkout', etiqueta: 'issue-26', area: 'loja' },
  {
    prototipo: 'confirmacao.html',
    rota: '/pedido-confirmado/1',
    etiqueta: 'issue-26',
    area: 'loja',
    api: { '/conta/pedidos/1': pedido },
  },
  { prototipo: 'conta-pagamentos.html', rota: '/conta/pagamentos', etiqueta: 'issue-28', area: 'loja' },
  { prototipo: 'conta-pedidos.html', rota: '/conta/pedidos', etiqueta: 'issue-29', area: 'loja' },
  { prototipo: 'suporte.html', rota: '/suporte', etiqueta: 'issue-38', area: 'loja' },
  { prototipo: 'empresas.html', rota: '/empresas', etiqueta: 'issue-empresas', area: 'loja' },
];
