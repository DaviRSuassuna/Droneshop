import { Routes } from '@angular/router';
import { adminAuthGuard } from './core/guards/admin-auth.guard';
import { TIPO_PRODUTO } from './core/models/produto.model';

export const routes: Routes = [
  { path: '', redirectTo: 'admin/marcas', pathMatch: 'full' },
  {
    path: 'admin',
    canActivateChild: [adminAuthGuard],
    children: [
      { path: '', redirectTo: 'marcas', pathMatch: 'full' },
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
        path: 'produtos',
        loadComponent: () =>
          import('./admin/catalogo/produtos/listagem/produtos-listagem').then(
            (m) => m.ProdutosListagem,
          ),
      },
      {
        path: 'drones/novo',
        data: { tipo: TIPO_PRODUTO.DRONE },
        loadComponent: () =>
          import('./admin/catalogo/produtos/cadastro/produto-cadastro').then((m) => m.ProdutoCadastro),
      },
      {
        path: 'drones/:id/editar',
        data: { tipo: TIPO_PRODUTO.DRONE },
        loadComponent: () =>
          import('./admin/catalogo/produtos/cadastro/produto-cadastro').then((m) => m.ProdutoCadastro),
      },
      {
        path: 'acessorios/novo',
        data: { tipo: TIPO_PRODUTO.ACESSORIO },
        loadComponent: () =>
          import('./admin/catalogo/produtos/cadastro/produto-cadastro').then((m) => m.ProdutoCadastro),
      },
      {
        path: 'acessorios/:id/editar',
        data: { tipo: TIPO_PRODUTO.ACESSORIO },
        loadComponent: () =>
          import('./admin/catalogo/produtos/cadastro/produto-cadastro').then((m) => m.ProdutoCadastro),
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
  {
    path: 'entrar',
    loadComponent: () =>
      import('./storefront/auth/login/login').then((m) => m.Login),
  },
  { path: '**', redirectTo: 'admin/marcas' },
];
