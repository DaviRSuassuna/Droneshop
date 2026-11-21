import { Routes } from '@angular/router';
import { clienteAuthGuard } from './core/guards/cliente-auth.guard';
import { adminAuthGuard } from './core/guards/admin-auth.guard';
import { AdminLayout } from './layout/admin/admin-layout';
import { TIPO_PRODUTO } from './core/models/produto.model';

export const routes: Routes = [
  { path: '', redirectTo: 'admin/marcas', pathMatch: 'full' },
  {
    path: 'admin',
    component: AdminLayout,
    canActivateChild: [adminAuthGuard],
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
  // loja do cliente (trilha A): Minha conta
  {
    path: 'conta',
    canActivate: [clienteAuthGuard],
    children: [
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
    ],
  },
  {
    path: 'entrar',
    loadComponent: () => import('./storefront/auth/login/login').then((m) => m.Login),
  },
  { path: '**', redirectTo: 'admin/marcas' },
];
