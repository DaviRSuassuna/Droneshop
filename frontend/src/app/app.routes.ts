import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home.component';
import { CatalogComponent } from './pages/catalog.component';
import { ProductComponent } from './pages/product.component';

export const routes: Routes = [
	{ path: '', component: HomeComponent },
	{ path: 'drones', component: CatalogComponent },
	{ path: 'produto/:id', component: ProductComponent },
	{ path: '**', redirectTo: '' },
];
