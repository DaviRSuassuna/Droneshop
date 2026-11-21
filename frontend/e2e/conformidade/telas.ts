import { expect, type Page } from '@playwright/test';

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
 * ignorar: textos do protótipo que são dado de exemplo, não estrutura da tela, ou que
 *          ainda não têm API (a #80 decidiu deixá-los fora da tela até virarem issue).
 */
export interface Tela {
  prototipo: string;
  rota: string;
  etiqueta: string;
  area: 'admin' | 'loja';
  api?: Record<string, unknown>;
  ignorar?: string[];
  /** Tabela inteira de recurso futuro sem API: colunas exatas e motivo explícito. */
  tabelasSemApi?: { colunas: string[]; motivo: string }[];
  /** Reproduz o estado interativo mostrado no protótipo, como seleção em lote. */
  preparar?: (page: Page) => Promise<void>;
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
      produto: { id: 1, sku: 'BAT-5000', nome: 'Bateria Inteligente 5000 mAh' },
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

const pedidoListagem = {
  id: pedido.id,
  numero: pedido.numero,
  status: pedido.status,
  cliente: pedido.cliente,
  valorTotal: pedido.valorTotal,
  quantidadeItens: pedido.itens.length,
  dataCadastro: '2026-10-24T12:00:00',
};
const historicoPedido = [
  {
    statusAnterior: null,
    statusNovo: { id: 1, nome: 'PENDENTE' },
    origem: { id: 2, nome: 'SISTEMA' },
    autor: null,
    observacao: null,
    dataCadastro: '2026-10-24T11:00:00',
  },
  {
    statusAnterior: { id: 1, nome: 'PENDENTE' },
    statusNovo: pedido.status,
    origem: { id: 1, nome: 'MANUAL' },
    autor: { id: 1, nome: 'Administrador de teste' },
    observacao: 'Conferência de teste',
    dataCadastro: '2026-10-24T12:00:00',
  },
];
const indicadoresDashboard = {
  periodo: { dataInicio: '2026-09-24', dataFim: '2026-10-24' },
  faturamento: 538.9,
  quantidadePedidos: 1,
  ticketMedio: 538.9,
  pedidosPorStatus: [{ status: pedido.status, quantidade: 1 }],
  estoqueBaixo: [
    {
      produto: { id: 1, nome: 'Bateria de teste', sku: 'BAT-5000' },
      disponivel: 1,
      estoqueMinimo: 5,
    },
  ],
  ultimosPedidos: [pedidoListagem],
  comparacao: {
    periodoAnterior: { dataInicio: '2026-08-25', dataFim: '2026-09-23' },
    faturamento: 480.0,
    quantidadePedidos: 1,
    ticketMedio: 480.0,
    variacaoFaturamento: 12.3,
    variacaoPedidos: 0.0,
    variacaoTicketMedio: 12.3,
  },
  vendasPorDia: [
    { data: '2026-10-23', faturamento: 0, quantidadePedidos: 0, faturamentoAnterior: 480.0, quantidadePedidosAnterior: 1 },
    { data: '2026-10-24', faturamento: 538.9, quantidadePedidos: 1, faturamentoAnterior: 0, quantidadePedidosAnterior: 0 },
  ],
  quantidadeAguardandoAcao: 1,
  pedidosAguardandoAcao: [{ ...pedidoListagem, status: { id: 1, nome: 'PENDENTE' } }],
  maisVendidos: [
    { produto: { id: 1, nome: 'Bateria de teste', sku: 'BAT-5000' }, unidades: 1, receita: 499.0, participacao: 92.6 },
  ],
  receitaPorCategoria: [{ categoria: 'Baterias', receita: 499.0, participacao: 100.0 }],
};

/** Nomes de produto do protótipo: dado de exemplo nos cards da loja (#21). */
const nomesDeExemplo = [
  'Falcon X2',
  'Falcon X2 Pro',
  'Nimbus Mini 3',
  'Heron Survey Pro',
  'Vortex FPV Racer',
  'Atlas Agro 10L',
  'Sparrow Kids',
  'Bateria extra Falcon X2',
  'Maleta rígida Falcon',
  'Hélices de reposição',
  'Antena de longo alcance',
  'Controle Aeria RC Pro',
  'Controle remoto',
  'Mochila de campo 25 L',
  'Carregador para 3 baterias',
  'Filtros ND (kit com 4)',
  'Gimbal de reposição',
  'Bateria Nimbus Mini 3',
];

/** Respostas da vitrine (#21): um card, as facetas e uma categoria com subcategoria. */
const vitrine = {
  '/loja/produtos': {
    items: [
      {
        id: 1,
        nome: 'Falcon X2 Pro',
        slug: 'falcon-x2-pro',
        tipo: { id: 1, nome: 'DRONE' },
        marca: { nome: 'Aerion' },
        precoMinimo: 12499,
        imagemPrincipal: null,
        disponivel: true,
      },
    ],
    page: 0,
    pageSize: 9,
    totalItems: 1,
    totalPages: 1,
  },
  '/loja/filtros': {
    marcas: [{ slug: 'aerion', nome: 'Aerion', quantidade: 1 }],
    preco: { min: 12499, max: 12499 },
  },
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
  sku: 'DRN-FX2P-0042',
  preco: 12499.0,
  disponivel: 5,
  grupo: {
    nome: 'Falcon X2',
    slug: 'falcon-x2',
    versoes: [{ id: 1, rotulo: 'Pro', slug: 'falcon-x2-pro', preco: 12499.0, disponivel: 5 }],
  },
  midias: [],
};

const grupo = {
  id: 1,
  nome: 'Falcon X2',
  slug: 'falcon-x2',
  descricaoCurta: 'Linha profissional dobrável.',
  ativo: true,
  precoAPartirDe: 12499.0,
  versoes: [
    {
      idProduto: 1,
      nome: 'Falcon X2 Pro',
      sku: 'DRN-FX2P-0042',
      tipo: { id: 1, nome: 'DRONE' },
      status: { id: 2, nome: 'Ativo' },
      rotulo: 'Pro',
      ordem: 1,
      preco: 12499.0,
    },
  ],
};

// Sem API ainda (decisão da #80): ficam fora da tela até virarem issue.
const SEM_API_PROMOCAO = [
  'Prioridade',
  'Chamada exibida na vitrine',
  'Teto de desconto',
  'Exibir na página Ofertas',
  'Selo Oferta nos cards',
  'Destacar no banner da home',
];
const SEM_API_CUPOM = [
  'Descrição interna',
  'Válido apenas para o primeiro pedido do cliente',
  'Acumula com promoções ativas',
  'Exibir na página Ofertas',
];

export const TELAS: Tela[] = [
  {
    prototipo: 'admin/configuracao.html',
    rota: '/admin/configuracao',
    etiqueta: 'issue-6',
    area: 'admin',
    api: {
      '/admin/configuracao': {
        id: 1,
        dadosFiscais: 'Loja de estudo',
        cepOrigemFrete: '77001000',
        remetentePadrao: 'Aeria Drones',
        prazoArrependimentoDias: 7,
        prazoGarantiaDias: 365,
        prazoAnaliseDevolucaoDiasUteis: 5,
        freteReversoPorContaLoja: true,
      },
    },
  },

  // ------------------------------------------------------------ admin
  {
    prototipo: 'admin/subcategorias.html',
    rota: '/admin/subcategorias',
    etiqueta: 'base',
    area: 'admin',
  },
  {
    prototipo: 'admin/subcategoria-nova.html',
    rota: '/admin/subcategorias/novo',
    etiqueta: 'base',
    area: 'admin',
  },
  { prototipo: 'admin/marcas.html', rota: '/admin/marcas', etiqueta: 'base', area: 'admin' },
  {
    prototipo: 'admin/marca-nova.html',
    rota: '/admin/marcas/novo',
    etiqueta: 'base',
    area: 'admin',
  },
  {
    prototipo: 'admin/categorias.html',
    rota: '/admin/categorias',
    etiqueta: 'base',
    area: 'admin',
  },
  {
    prototipo: 'admin/categoria-nova.html',
    rota: '/admin/categorias/novo',
    etiqueta: 'base',
    area: 'admin',
  },
  {
    prototipo: 'admin/fornecedores.html',
    rota: '/admin/fornecedores',
    etiqueta: 'base',
    area: 'admin',
  },
  {
    prototipo: 'admin/fornecedor-novo.html',
    rota: '/admin/fornecedores/novo',
    etiqueta: 'base',
    area: 'admin',
  },
  {
    prototipo: 'admin/produtos.html',
    rota: '/admin/produtos',
    etiqueta: 'issue-8',
    area: 'admin',
    api: {
      '/produtos': {
        items: [
          { ...produto, status: { id: 2, nome: 'Ativo' }, estoqueFisico: 5, estoqueMinimo: 2 },
        ],
        page: 0,
        pageSize: 10,
        totalItems: 1,
        totalPages: 1,
      },
    },
    preparar: async (page) => {
      await page.getByRole('checkbox', { name: 'Selecionar ' + produto.nome, exact: true }).check();
      await expect(page.getByRole('button', { name: 'Ativar', exact: true })).toBeVisible();
    },
    // Exclusão em lote ainda não faz parte do contrato.
    ignorar: ['Excluir'],
  },
  {
    prototipo: 'admin/produto-novo.html',
    rota: '/admin/drones/novo',
    etiqueta: 'issue-8',
    area: 'admin',
  },
  {
    prototipo: 'admin/acessorio-novo.html',
    rota: '/admin/acessorios/novo',
    etiqueta: 'issue-8',
    area: 'admin',
  },
  {
    prototipo: 'admin/kit-novo.html',
    rota: '/admin/kits/novo',
    etiqueta: 'issue-80',
    area: 'admin',
  },
  { prototipo: 'admin/grupos.html', rota: '/admin/grupos', etiqueta: 'issue-80', area: 'admin' },
  {
    prototipo: 'admin/grupo-novo.html',
    rota: '/admin/grupos/1/editar',
    etiqueta: 'issue-80',
    area: 'admin',
    api: { '/grupos-produto/1': grupo },
    // versões entram e saem do grupo pelo cadastro do produto (idGrupo, rotuloNoGrupo)
    ignorar: ['Adicionar produto ao grupo'],
  },
  {
    prototipo: 'admin/usuarios.html',
    rota: '/admin/usuarios',
    etiqueta: 'issue-5',
    area: 'admin',
    // Auditoria ainda não tem endpoint; status administrativo atual é ativo/inativo.
    ignorar: ['Ver log de auditoria'],
  },
  {
    prototipo: 'admin/usuario-novo.html',
    rota: '/admin/usuarios/novo',
    etiqueta: 'issue-5',
    area: 'admin',
    ignorar: [
      // Convite entra com a #33; 2FA não faz parte da API atual de usuários.
      'Enviar convite ao criar',
    ],
  },
  {
    prototipo: 'admin/estoque.html',
    rota: '/admin/estoque',
    etiqueta: 'issue-13',
    area: 'admin',
    // reservas (#23) e números de série (#34) ainda não existem
    ignorar: ['Reservas ativas', 'Números de série'],
  },
  {
    prototipo: 'admin/estoque-movimento.html',
    rota: '/admin/estoque/movimento',
    etiqueta: 'issue-13',
    area: 'admin',
  },
  {
    prototipo: 'admin/promocoes.html',
    rota: '/admin/promocoes',
    etiqueta: 'issue-14',
    area: 'admin',
    api: {
      '/promocoes': {
        items: [
          {
            id: 1,
            nome: 'Semana do piloto',
            escopo: { id: 1, nome: 'PRODUTO' },
            dataInicio: '2026-10-20T00:00:00',
            dataFim: '2026-10-26T23:59:59',
            percentualDesconto: 22,
            valorDesconto: null,
            valorMinimoCarrinho: null,
            ativa: true,
            situacao: 'VIGENTE',
            produtos: [{ id: 1, nome: 'Drone de teste', sku: 'TESTE' }],
          },
        ],
        page: 0,
        pageSize: 1000,
        totalItems: 1,
        totalPages: 1,
      },
    },
  },
  {
    prototipo: 'admin/promocao-nova.html',
    rota: '/admin/promocoes/nova',
    etiqueta: 'issue-14',
    area: 'admin',
    ignorar: SEM_API_PROMOCAO,
  },
  {
    // Davi autorizou Exportar usos em cada cupom, usando /cupons/{id}/usos.
    prototipo: 'admin/cupons.html',
    rota: '/admin/cupons',
    etiqueta: 'issue-15',
    area: 'admin',
    api: {
      '/cupons': {
        items: [
          {
            id: 1,
            codigo: 'PILOTO10',
            tipoDesconto: { id: 1, nome: 'PERCENTUAL' },
            valor: 10,
            dataInicio: '2026-10-01T00:00:00',
            dataFim: null,
            valorMinimo: null,
            limiteTotalUso: 500,
            limitePorCliente: 1,
            ativo: true,
            situacao: 'VIGENTE',
            usos: 184,
            categorias: [],
            marcas: [],
            produtos: [],
          },
        ],
        page: 0,
        pageSize: 1000,
        totalItems: 1,
        totalPages: 1,
      },
    },
  },
  {
    prototipo: 'admin/cupom-novo.html',
    rota: '/admin/cupons/novo',
    etiqueta: 'issue-15',
    area: 'admin',
    ignorar: SEM_API_CUPOM,
  },
  {
    prototipo: 'admin/pedidos.html',
    rota: '/admin/pedidos',
    etiqueta: 'issue-16',
    area: 'admin',
    api: {
      '/pedidos': { items: [pedidoListagem], page: 0, pageSize: 10, totalItems: 1, totalPages: 1 },
      '/pedidos/resumo-status': [{ status: pedido.status, quantidade: 1 }],
    },
    // Contrato atual: número/nome, contagens por status e etiqueta individual. Sem métricas logísticas.
    ignorar: [
      'Imprimir etiquetas',
      'Devoluções abertas',
      'Buscar por número, cliente ou CPF/CNPJ',
      'Período: 30 dias',
      'Pagamento',
    ],
  },
  {
    prototipo: 'admin/pedido.html',
    rota: '/admin/pedidos/1',
    etiqueta: 'issue-16',
    area: 'admin',
    api: { '/pedidos/1': pedido, '/pedidos/1/historico': historicoPedido },
  },
  {
    prototipo: 'admin/index.html',
    rota: '/admin',
    etiqueta: 'issue-18',
    area: 'admin',
    api: {
      '/admin/dashboard': indicadoresDashboard,
      '/pedidos': {
        items: [{ ...pedidoListagem, status: { id: 1, nome: 'PENDENTE' } }],
        page: 0,
        pageSize: 5,
        totalItems: 1,
        totalPages: 1,
      },
    },
    // Série, comparação, aguardando ação, mais vendidos e receita por categoria entraram com a #85.
  },

  // ------------------------------------------------------------ loja
  {
    prototipo: 'index.html',
    rota: '/',
    etiqueta: 'issue-21',
    area: 'loja',
    api: vitrine,
    // Nomes de produto são dado de exemplo; "O que você vai voar?" lista as subcategorias de
    // drone da API no lugar dos desenhos fixos do protótipo (desenhos.js).
    ignorar: [...nomesDeExemplo, 'Filmagem e viagem', 'Inspeção', 'Mapeamento', 'Agricultura'],
  },
  {
    prototipo: 'drones.html',
    rota: '/drones',
    etiqueta: 'issue-21',
    area: 'loja',
    api: vitrine,
    // Marcas e "Uso" vêm de /loja/filtros e /loja/categorias (dado). "Escolher versão" pede saber
    // se o item é um grupo, o que a listagem não traz; o card sempre leva à página da versão.
    // Registro ANAC não é filtro do contrato da #21.
    ignorar: [
      ...nomesDeExemplo,
      'Escolher versão',
      'Aerion',
      'Skyline',
      'Vortex',
      'Atlas',
      'Heron',
      'Sparrow',
      'Profissional',
      'Lazer',
      'Corrida FPV',
      'Agro / mapeamento',
      'Exige SISANT',
    ],
  },
  {
    prototipo: 'acessorios.html',
    rota: '/acessorios',
    etiqueta: 'issue-21',
    area: 'loja',
    api: vitrine,
    // "Tipo" vem das subcategorias de acessório (dado). Compatibilidade e disponibilidade
    // (pronta entrega, pré-venda) não são filtros do contrato da #21.
    ignorar: [
      ...nomesDeExemplo,
      'Baterias',
      'Hélices',
      'Maletas e mochilas',
      'Filtros e lentes',
      'Controles e antenas',
      'Carregadores',
      'Falcon X2',
      'Nimbus Mini 3',
      'Vortex FPV',
      'Atlas Agro',
      'Heron Survey',
      'Pronta entrega',
      'Pré-venda',
    ],
  },
  {
    prototipo: 'produto.html',
    rota: '/produto/falcon-x2-pro',
    etiqueta: 'issue-21',
    area: 'loja',
    api: {
      '/loja/produtos/falcon-x2-pro': produto,
      '/loja/produtos/falcon-x2-pro/compativeis': [
        { id: 9, nome: 'Bateria extra Falcon X2', slug: 'bateria-falcon-x2', precoMinimo: 899 },
      ],
    },
    // Comparação entra com a #44; avaliações não têm issue no contrato atual.
    ignorar: [
      ...nomesDeExemplo,
      'Comparar com outro drone',
      'Avaliar este drone',
      'Cancelar',
      'Publicar avaliação',
      'Escolha um drone',
      'Quem já voa com ele.',
      'Sua nota',
      '★',
      'Título',
      'Comentário',
      'Uso principal',
      'Fotos ou vídeo',
      'Adicionar arquivos',
    ],
    tabelasSemApi: [{ colunas: ['Modelo'], motivo: 'Comparação de especificações: #44' }],
  },
  {
    prototipo: 'ofertas.html',
    rota: '/ofertas',
    etiqueta: 'issue-21',
    area: 'loja',
    api: {
      '/loja/ofertas': [
        {
          produto: { id: 1, nome: 'Nimbus Mini 3', slug: 'nimbus-mini-3', precoMinimo: 4790 },
          precoDe: 4790,
          precoPor: 4290,
          promocao: { id: 1, nome: 'Semana do piloto', dataFim: '2099-10-26T23:59:00' },
        },
      ],
    },
    ignorar: [...nomesDeExemplo],
  },
  {
    prototipo: 'login.html',
    rota: '/entrar',
    etiqueta: 'issue-4',
    area: 'loja',
    // Busca da loja (#43), recuperação de senha e acesso fictício não existem na tela atual.
    ignorar: ['Buscar', 'Esqueci minha senha', 'Continuar como Marina (protótipo)'],
  },
  {
    prototipo: 'conta-perfil.html',
    rota: '/conta/perfil',
    etiqueta: 'issue-19',
    area: 'loja',
    api: {
      '/conta/perfil': {
        nome: 'Marina Ribeiro',
        email: 'marina@exemplo.com',
        tipoPessoa: { id: 1, nome: 'PF' },
        documento: '52998224725',
        telefone: '63988124477',
      },
    },
    // sem campo no contrato da #19: nascimento e perfil de piloto (#83), novidades (#42), drones (#88)
    ignorar: [
      'Data de nascimento',
      'Quero receber novidades, ofertas e conteúdos sobre voo',
      'Perfil de piloto',
      'Drones registrados',
    ],
  },
  {
    prototipo: 'conta-enderecos.html',
    rota: '/conta/enderecos',
    etiqueta: 'issue-19',
    area: 'loja',
    // sem campo no contrato da #19 (o endereço do cliente não tem destinatário, telefone nem cobrança)
    ignorar: ['Quem recebe', 'Telefone para entrega', 'Usar também como endereço de cobrança'],
  },
  {
    prototipo: 'conta-favoritos.html',
    rota: '/conta/favoritos',
    etiqueta: 'issue-22',
    area: 'loja',
  },
  { prototipo: 'carrinho.html', rota: '/carrinho', etiqueta: 'issue-24', area: 'loja' },
  { prototipo: 'checkout.html', rota: '/checkout', etiqueta: 'issue-26', area: 'loja' },
  {
    prototipo: 'confirmacao.html',
    rota: '/pedido-confirmado/1',
    etiqueta: 'issue-26',
    area: 'loja',
    api: { '/conta/pedidos/1': pedido },
  },
  {
    prototipo: 'conta-pagamentos.html',
    rota: '/conta/pagamentos',
    etiqueta: 'issue-28',
    area: 'loja',
  },
  { prototipo: 'conta-pedidos.html', rota: '/conta/pedidos', etiqueta: 'issue-29', area: 'loja' },
  {
    prototipo: 'suporte.html',
    rota: '/suporte',
    etiqueta: 'issue-38',
    area: 'loja',
    // anexo no chamado não tem API (o contrato da #38 é só texto)
    ignorar: ['Fotos ou vídeo'],
  },
  { prototipo: 'empresas.html', rota: '/empresas', etiqueta: 'issue-empresas', area: 'loja' },
];
