import { Component, ElementRef, Injector, afterNextRender, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { filter } from 'rxjs';

interface ItemNav {
  rotulo: string;
  rota: string;
  /** Só marca como ativo na rota exata (o Dashboard é /admin, prefixo de todas as outras). */
  exata?: boolean;
}

// Barra superior do painel (.tb do protótipo): abas no desktop, menu rolável (.mnav) no celular.
// Subcategorias e Configuração não estão no protótipo, mas são o único caminho até essas telas.
@Component({
  selector: 'app-admin-nav',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './admin-nav.html',
})
export class AdminNav {
  readonly itens: ItemNav[] = [
    { rotulo: 'Dashboard', rota: '/admin', exata: true },
    { rotulo: 'Produtos', rota: '/admin/produtos' },
    { rotulo: 'Marcas', rota: '/admin/marcas' },
    { rotulo: 'Categorias', rota: '/admin/categorias' },
    { rotulo: 'Subcategorias', rota: '/admin/subcategorias' },
    { rotulo: 'Fornecedores', rota: '/admin/fornecedores' },
    { rotulo: 'Promoções', rota: '/admin/promocoes' },
    { rotulo: 'Cupons', rota: '/admin/cupons' },
    { rotulo: 'Estoque', rota: '/admin/estoque' },
    { rotulo: 'Pedidos', rota: '/admin/pedidos' },
    { rotulo: 'Usuários', rota: '/admin/usuarios' },
    { rotulo: 'Configuração', rota: '/admin/configuracao' },
  ];

  constructor() {
    // como no protótipo: a aba atual fica centralizada quando o menu rola para o lado.
    // A barra fica no layout e não é recriada, então recentraliza a cada navegação.
    const host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
    const injector = inject(Injector);
    const centralizar = () =>
      host.querySelectorAll<HTMLElement>('.tabs a[aria-current], .mnav a[aria-current]').forEach((a) => {
        const menu = a.parentElement;
        if (menu) menu.scrollLeft = a.offsetLeft - (menu.clientWidth - a.offsetWidth) / 2;
      });
    afterNextRender(centralizar);
    inject(Router)
      .events.pipe(
        filter((e) => e instanceof NavigationEnd),
        takeUntilDestroyed(),
      )
      .subscribe(() => afterNextRender(centralizar, { injector }));
  }
}
