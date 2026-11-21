import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { BrilhoCardsDirective } from '../../shared/directives/brilho-cards.directive';
import { AdminNav } from './admin-nav/admin-nav';

// Moldura das telas do painel: navegação compartilhada e conteúdo de cada rota no outlet.
// Os efeitos do protótipo 02 (admin-fx) vêm de styles/efeitos-admin; o brilho dos cards
// precisa do ponteiro, por isso a diretiva fica aqui.
@Component({
  selector: 'app-admin-layout',
  imports: [AdminNav, RouterOutlet],
  hostDirectives: [BrilhoCardsDirective],
  template: `<app-admin-nav /><router-outlet />`,
})
export class AdminLayout {}
