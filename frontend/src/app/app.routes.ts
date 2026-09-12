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
  { path: '**', redirectTo: 'admin/marcas' },
];
