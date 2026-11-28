import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { papelGuard } from './core/guards/papel.guard';
import { AdminLayout } from './layout/admin/admin-layout';
import { StorefrontLayout } from './layout/storefront/storefront-layout';
import { TIPO_PRODUTO } from './core/models/produto.model';

export const routes: Routes = [
  {
    path: 'admin',
    component: AdminLayout,
    canActivateChild: [authGuard, papelGuard(['ADMINISTRATIVO'])],
    children: [
      {
        path: '',
        pathMatch: 'full',
        loadComponent: () => import('./admin/dashboard/dashboard').then((m) => m.Dashboard),
      },
      {
        path: 'marcas',
        loadComponent: () =>
          import('./admin/catalogo/marcas/listagem/marcas-listagem').then((m) => m.MarcasListagem),
      },
      {
        path: 'marcas/novo',
        loadComponent: () =>
          import('./admin/catalogo/marcas/cadastro/marca-cadastro').then((m) => m.MarcaCadastro),
      },
      {
        path: 'marcas/:id/editar',
        loadComponent: () =>
          import('./admin/catalogo/marcas/cadastro/marca-cadastro').then((m) => m.MarcaCadastro),
      },
      {
        path: 'categorias',
        loadComponent: () =>
          import('./admin/catalogo/categorias/listagem/categorias-listagem').then(
            (m) => m.CategoriasListagem,
          ),
      },
      {
        path: 'categorias/novo',
        loadComponent: () =>
          import('./admin/catalogo/categorias/cadastro/categoria-cadastro').then(
            (m) => m.CategoriaCadastro,
          ),
      },
      {
        path: 'categorias/:id/editar',
        loadComponent: () =>
          import('./admin/catalogo/categorias/cadastro/categoria-cadastro').then(
            (m) => m.CategoriaCadastro,
          ),
      },
      {
        path: 'subcategorias',
        loadComponent: () =>
          import('./admin/catalogo/subcategorias/listagem/subcategorias-listagem').then(
            (m) => m.SubcategoriasListagem,
          ),
      },
      {
        path: 'subcategorias/novo',
        loadComponent: () =>
          import('./admin/catalogo/subcategorias/cadastro/subcategoria-cadastro').then(
            (m) => m.SubcategoriaCadastro,
          ),
      },
      {
        path: 'subcategorias/:id/editar',
        loadComponent: () =>
          import('./admin/catalogo/subcategorias/cadastro/subcategoria-cadastro').then(
            (m) => m.SubcategoriaCadastro,
          ),
      },
      {
        path: 'fornecedores',
        loadComponent: () =>
          import('./admin/catalogo/fornecedores/listagem/fornecedores-listagem').then(
            (m) => m.FornecedoresListagem,
          ),
      },
      {
        path: 'fornecedores/novo',
        loadComponent: () =>
          import('./admin/catalogo/fornecedores/cadastro/fornecedor-cadastro').then(
            (m) => m.FornecedorCadastro,
          ),
      },
      {
        path: 'fornecedores/:id/editar',
        loadComponent: () =>
          import('./admin/catalogo/fornecedores/cadastro/fornecedor-cadastro').then(
            (m) => m.FornecedorCadastro,
          ),
      },
      {
        path: 'promocoes',
        loadComponent: () =>
          import('./admin/promocoes/listagem/promocoes-listagem').then((m) => m.PromocoesListagem),
      },
      {
        path: 'promocoes/nova',
        loadComponent: () =>
          import('./admin/promocoes/cadastro/promocao-cadastro').then((m) => m.PromocaoCadastro),
      },
      {
        path: 'promocoes/:id/editar',
        loadComponent: () =>
          import('./admin/promocoes/cadastro/promocao-cadastro').then((m) => m.PromocaoCadastro),
      },
      {
        path: 'produtos',
        loadComponent: () =>
          import('./admin/catalogo/produtos/listagem/produtos-listagem').then(
            (m) => m.ProdutosListagem,
          ),
      },
      {
        path: 'cupons',
        loadComponent: () =>
          import('./admin/cupons/listagem/cupons-listagem').then((m) => m.CuponsListagem),
      },
      {
        path: 'cupons/novo',
        loadComponent: () =>
          import('./admin/cupons/cadastro/cupom-cadastro').then((m) => m.CupomCadastro),
      },
      {
        path: 'cupons/:id/editar',
        loadComponent: () =>
          import('./admin/cupons/cadastro/cupom-cadastro').then((m) => m.CupomCadastro),
      },
      // drone e acessório usam a mesma tela de cadastro; o tipo vem do data da rota (D27)
      {
        path: 'drones/novo',
        data: { tipo: TIPO_PRODUTO.DRONE },
        loadComponent: () =>
          import('./admin/catalogo/produtos/cadastro/produto-cadastro').then(
            (m) => m.ProdutoCadastro,
          ),
      },
      {
        path: 'drones/:id/editar',
        data: { tipo: TIPO_PRODUTO.DRONE },
        loadComponent: () =>
          import('./admin/catalogo/produtos/cadastro/produto-cadastro').then(
            (m) => m.ProdutoCadastro,
          ),
      },
      {
        path: 'acessorios/novo',
        data: { tipo: TIPO_PRODUTO.ACESSORIO },
        loadComponent: () =>
          import('./admin/catalogo/produtos/cadastro/produto-cadastro').then(
            (m) => m.ProdutoCadastro,
          ),
      },
      {
        path: 'acessorios/:id/editar',
        data: { tipo: TIPO_PRODUTO.ACESSORIO },
        loadComponent: () =>
          import('./admin/catalogo/produtos/cadastro/produto-cadastro').then(
            (m) => m.ProdutoCadastro,
          ),
      },
      {
        path: 'grupos',
        loadComponent: () =>
          import('./admin/catalogo/grupos/listagem/grupos-listagem').then((m) => m.GruposListagem),
      },
      {
        path: 'grupos/novo',
        loadComponent: () =>
          import('./admin/catalogo/grupos/cadastro/grupo-cadastro').then((m) => m.GrupoCadastro),
      },
      {
        path: 'grupos/:id/editar',
        loadComponent: () =>
          import('./admin/catalogo/grupos/cadastro/grupo-cadastro').then((m) => m.GrupoCadastro),
      },
      {
        path: 'kits/novo',
        data: { tipo: TIPO_PRODUTO.KIT },
        loadComponent: () =>
          import('./admin/catalogo/produtos/cadastro/produto-cadastro').then(
            (m) => m.ProdutoCadastro,
          ),
      },
      {
        path: 'kits/:id/editar',
        data: { tipo: TIPO_PRODUTO.KIT },
        loadComponent: () =>
          import('./admin/catalogo/produtos/cadastro/produto-cadastro').then(
            (m) => m.ProdutoCadastro,
          ),
      },
      {
        path: 'termos-buscados',
        loadComponent: () =>
          import('./admin/catalogo/termos-buscados/termos-buscados').then((m) => m.TermosBuscadosPage),
      },
      {
        path: 'auditoria',
        loadComponent: () =>
          import('./admin/auditoria/auditoria').then((m) => m.AuditoriaPage),
      },
      {
        path: 'chamados',
        loadComponent: () =>
          import('./admin/atendimento/chamados/chamados').then((m) => m.ChamadosPage),
      },
      {
        path: 'moderacao',
        loadComponent: () =>
          import('./admin/atendimento/moderacao/moderacao').then((m) => m.ModeracaoPage),
      },
      {
        path: 'leads-empresas',
        loadComponent: () =>
          import('./admin/atendimento/leads/leads-empresas').then((m) => m.LeadsEmpresasPage),
      },
      {
        path: 'templates-email',
        loadComponent: () =>
          import('./admin/notificacoes/templates-email').then((m) => m.TemplatesEmailPage),
      },
      {
        path: 'configuracao',
        loadComponent: () =>
          import('./admin/configuracao/configuracao-loja').then((m) => m.ConfiguracaoLojaPage),
      },
      {
        path: 'estoque',
        loadComponent: () =>
          import('./admin/estoque/listagem/estoque-listagem').then((m) => m.EstoqueListagem),
      },
      {
        path: 'estoque/movimento',
        loadComponent: () =>
          import('./admin/estoque/movimento/estoque-movimento').then((m) => m.EstoqueMovimento),
      },
      {
        path: 'pedidos',
        loadComponent: () =>
          import('./admin/pedidos/listagem/pedidos-listagem').then((m) => m.PedidosListagem),
      },
      {
        path: 'pedidos/:id',
        loadComponent: () =>
          import('./admin/pedidos/detalhe/pedido-detalhe').then((m) => m.PedidoDetalhe),
      },
      {
        path: 'usuarios',
        loadComponent: () =>
          import('./admin/usuarios/listagem/usuarios-listagem').then((m) => m.UsuariosListagem),
      },
      {
        path: 'usuarios/novo',
        loadComponent: () =>
          import('./admin/usuarios/cadastro/usuario-cadastro').then((m) => m.UsuarioCadastro),
      },
      {
        path: 'usuarios/:id/editar',
        loadComponent: () =>
          import('./admin/usuarios/cadastro/usuario-cadastro').then((m) => m.UsuarioCadastro),
      },
    ],
  },
  // Loja do catálogo (trilha B): telas dentro da casca da loja (#96)
  {
    path: '',
    component: StorefrontLayout,
    children: [
      {
        path: '',
        pathMatch: 'full',
        loadComponent: () => import('./storefront/home/home').then((m) => m.Home),
      },
      {
        path: 'suporte',
        loadComponent: () =>
          import('./storefront/suporte/suporte').then((m) => m.Suporte),
      },
      {
        path: 'empresas',
        loadComponent: () =>
          import('./storefront/empresas/empresas').then((m) => m.Empresas),
      },
      {
        path: 'descadastro/:token',
        loadComponent: () =>
          import('./storefront/notificacoes/descadastro').then((m) => m.Descadastro),
      },
      {
        path: 'busca',
        loadComponent: () =>
          import('./storefront/catalogo/busca/busca').then((m) => m.BuscaPage),
      },
      {
        path: 'drones',
        data: { tipo: 'DRONE' },
        loadComponent: () =>
          import('./storefront/catalogo/listagem/catalogo-listagem').then(
            (m) => m.CatalogoListagem,
          ),
      },
      {
        path: 'acessorios',
        data: { tipo: 'ACESSORIO' },
        loadComponent: () =>
          import('./storefront/catalogo/listagem/catalogo-listagem').then(
            (m) => m.CatalogoListagem,
          ),
      },
      {
        path: 'produto/:slug',
        loadComponent: () => import('./storefront/catalogo/produto/produto').then((m) => m.Produto),
      },
      {
        path: 'ofertas',
        loadComponent: () => import('./storefront/catalogo/ofertas/ofertas').then((m) => m.Ofertas),
      },
      // loja do cliente (trilha A): Minha conta
      {
        path: 'conta',
        canActivateChild: [authGuard, papelGuard(['CLIENTE'])],
        children: [
          { path: '', pathMatch: 'full', redirectTo: 'perfil' },
          {
            path: 'perfil',
            loadComponent: () =>
              import('./storefront/conta/perfil/conta-perfil').then((m) => m.ContaPerfil),
          },
          {
            path: 'enderecos',
            loadComponent: () =>
              import('./storefront/conta/enderecos/conta-enderecos').then((m) => m.ContaEnderecos),
          },
          {
            path: 'favoritos',
            loadComponent: () =>
              import('./storefront/compra/favoritos/conta-favoritos').then((m) => m.ContaFavoritos),
          },
          {
            path: 'pedidos',
            loadComponent: () =>
              import('./storefront/conta/pedidos/conta-pedidos').then((m) => m.ContaPedidos),
          },
          {
            path: 'pagamentos',
            loadComponent: () =>
              import('./storefront/conta/pagamentos/conta-pagamentos').then(
                (m) => m.ContaPagamentos,
              ),
          },
          {
            path: 'drones',
            loadComponent: () =>
              import('./storefront/conta/drones/conta-drones').then((m) => m.ContaDrones),
          },
          {
            path: 'notificacoes',
            loadComponent: () =>
              import('./storefront/notificacoes/conta-notificacoes').then(
                (m) => m.ContaNotificacoes,
              ),
          },
        ],
      },
      {
        path: 'carrinho',
        loadComponent: () =>
          import('./storefront/compra/carrinho/carrinho').then((m) => m.CarrinhoPagina),
      },
      {
        path: 'checkout',
        canActivate: [authGuard, papelGuard(['CLIENTE'])],
        loadComponent: () =>
          import('./storefront/compra/checkout/checkout').then((m) => m.Checkout),
      },
      {
        path: 'pedido-confirmado/:id',
        canActivate: [authGuard, papelGuard(['CLIENTE'])],
        loadComponent: () =>
          import('./storefront/compra/confirmacao/confirmacao').then((m) => m.Confirmacao),
      },
      {
        path: 'acesso-negado',
        title: 'Acesso negado | Aeria Drones',
        loadComponent: () =>
          import('./storefront/auth/acesso-negado').then((m) => m.AcessoNegado),
      },
      {
        path: 'entrar',
        loadComponent: () => import('./storefront/auth/login/login').then((m) => m.Login),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
