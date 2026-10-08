import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'admin/marcas', pathMatch: 'full' },
  {
    path: 'admin/marcas',
    loadComponent: () =>
      import('./admin/catalogo/marcas/listagem/marcas-listagem').then((m) => m.MarcasListagem),
  },
  {
    path: 'admin/marcas/novo',
    loadComponent: () =>
      import('./admin/catalogo/marcas/cadastro/marca-cadastro').then((m) => m.MarcaCadastro),
  },
  {
    path: 'admin/marcas/:id/editar',
    loadComponent: () =>
      import('./admin/catalogo/marcas/cadastro/marca-cadastro').then((m) => m.MarcaCadastro),
  },
  {
    path: 'admin/categorias',
    loadComponent: () =>
      import('./admin/catalogo/categorias/listagem/categorias-listagem').then(
        (m) => m.CategoriasListagem,
      ),
  },
  {
    path: 'admin/categorias/novo',
    loadComponent: () =>
      import('./admin/catalogo/categorias/cadastro/categoria-cadastro').then(
        (m) => m.CategoriaCadastro,
      ),
  },
  {
    path: 'admin/categorias/:id/editar',
    loadComponent: () =>
      import('./admin/catalogo/categorias/cadastro/categoria-cadastro').then(
        (m) => m.CategoriaCadastro,
      ),
  },
  {
    path: 'admin/subcategorias',
    loadComponent: () =>
      import('./admin/catalogo/subcategorias/listagem/subcategorias-listagem').then(
        (m) => m.SubcategoriasListagem,
      ),
  },
  {
    path: 'admin/subcategorias/novo',
    loadComponent: () =>
      import('./admin/catalogo/subcategorias/cadastro/subcategoria-cadastro').then(
        (m) => m.SubcategoriaCadastro,
      ),
  },
  {
    path: 'admin/subcategorias/:id/editar',
    loadComponent: () =>
      import('./admin/catalogo/subcategorias/cadastro/subcategoria-cadastro').then(
        (m) => m.SubcategoriaCadastro,
      ),
  },
  {
    path: 'admin/fornecedores',
    loadComponent: () =>
      import('./admin/catalogo/fornecedores/listagem/fornecedores-listagem').then(
        (m) => m.FornecedoresListagem,
      ),
  },
  {
    path: 'admin/fornecedores/novo',
    loadComponent: () =>
      import('./admin/catalogo/fornecedores/cadastro/fornecedor-cadastro').then(
        (m) => m.FornecedorCadastro,
      ),
  },
  {
    path: 'admin/fornecedores/:id/editar',
    loadComponent: () =>
      import('./admin/catalogo/fornecedores/cadastro/fornecedor-cadastro').then(
        (m) => m.FornecedorCadastro,
      ),
  },
  {
    path: 'admin/produtos',
    loadComponent: () =>
      import('./admin/catalogo/produtos/listagem/produtos-listagem').then(
        (m) => m.ProdutosListagem,
      ),
  },
  {
    path: 'admin/produtos/novo',
    loadComponent: () =>
      import('./admin/catalogo/produtos/cadastro/produto-cadastro').then((m) => m.ProdutoCadastro),
  },
  {
    path: 'admin/produtos/:id/editar',
    loadComponent: () =>
      import('./admin/catalogo/produtos/cadastro/produto-cadastro').then((m) => m.ProdutoCadastro),
  },
  { path: '**', redirectTo: 'admin/marcas' },
];
