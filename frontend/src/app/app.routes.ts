import { Routes } from '@angular/router';
<<<<<<< HEAD

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
=======
import { HomeComponent } from './pages/home.component';
import { CatalogComponent } from './pages/catalog.component';
import { ProductComponent } from './pages/product.component';

export const routes: Routes = [
	{ path: '', component: HomeComponent },
	{ path: 'drones', component: CatalogComponent },
	{ path: 'produto/:id', component: ProductComponent },
	{ path: '**', redirectTo: '' },
>>>>>>> ed3d54b (Criação do frontend(telas) com o Angular)
];
