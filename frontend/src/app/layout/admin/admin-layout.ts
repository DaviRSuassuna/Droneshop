import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AdminNav } from './admin-nav/admin-nav';

// Moldura das telas do painel: navegação compartilhada e conteúdo de cada rota no outlet.
@Component({
  selector: 'app-admin-layout',
  imports: [AdminNav, RouterOutlet],
  template: `<app-admin-nav /><router-outlet />`,
})
export class AdminLayout {}
