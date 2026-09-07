import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { ThemeService } from '../core/theme.service';

@Component({
  selector: 'app-storefront-header',
  imports: [RouterLink, RouterLinkActive],
  template: `
    <header class="store-header">
      <div class="header-inner">
        <a class="brand" routerLink="/">AERIA<span>/</span></a>
        <nav aria-label="Navegacao principal">
          <a routerLink="/drones" routerLinkActive="active">Drones</a>
          <a routerLink="/drones" [queryParams]="{ categoria: 'acessorios' }">Acessorios</a>
          <a routerLink="/" fragment="corporativo">Empresas</a>
        </nav>
        <div class="header-actions">
          <button class="theme-button" type="button" (click)="theme.toggle()" [attr.aria-label]="theme.dark() ? 'Ativar tema claro' : 'Ativar tema escuro'">{{ theme.dark() ? 'claro' : 'escuro' }}</button>
          <a routerLink="/" fragment="conta">Conta</a>
          <a class="cart-link" routerLink="/" fragment="carrinho">Carrinho <b>0</b></a>
        </div>
      </div>
    </header>
  `,
  styles: [`
    .store-header { position:sticky; top:0; z-index:10; border-bottom:1px solid var(--line); background:rgba(10,11,13,.84); backdrop-filter:blur(16px); }
    .header-inner { width:min(1180px,calc(100% - 48px)); min-height:66px; margin:auto; display:flex; align-items:center; gap:38px; }
    .brand { font:500 20px/1 'Space Grotesk',sans-serif; letter-spacing:.14em; }.brand span { color:var(--accent); }
    nav { display:flex; align-items:center; gap:25px; flex:1; } nav a,.header-actions a,.theme-button { color:var(--muted); font-size:12px; background:none; border:0; padding:8px 0; } nav a:hover,nav a.active,.header-actions a:hover { color:var(--text); }
    .header-actions { display:flex; align-items:center; gap:18px; }.theme-button { border-bottom:1px solid var(--line); }
    .cart-link { display:flex; gap:8px; align-items:center; }.cart-link b { display:grid; place-items:center; width:19px; height:19px; border-radius:50%; background:var(--accent); color:var(--accent-ink); font-size:10px; }
    @media (max-width:760px) { .header-inner { width:min(100% - 28px,1180px); gap:18px; }.header-inner nav { display:none; }.header-actions { margin-left:auto; gap:11px; }.theme-button { display:none; } }
  `],
})
export class StorefrontHeaderComponent { protected readonly theme = inject(ThemeService); }