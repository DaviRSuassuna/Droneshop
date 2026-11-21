import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { FaixaPromocao } from './faixa-promocao/faixa-promocao';
import { StorefrontFooter } from './storefront-footer/storefront-footer';
import { StorefrontNav } from './storefront-nav/storefront-nav';

// Casca da loja (protótipo 02): faixa de promoção, menu, conteúdo de cada rota e rodapé.
@Component({
  selector: 'app-storefront-layout',
  imports: [FaixaPromocao, StorefrontNav, StorefrontFooter, RouterOutlet],
  template: `
    <app-faixa-promocao />
    <app-storefront-nav />
    <router-outlet />
    <app-storefront-footer />
  `,
})
export class StorefrontLayout {}
