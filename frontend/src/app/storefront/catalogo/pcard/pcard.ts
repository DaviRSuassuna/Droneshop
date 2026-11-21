import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ProdutoLojaItem } from '../../../core/models/loja.model';
import { PrecoPipe } from '../preco.pipe';
import { CardSpotlightDirective } from '../../../shared/directives/card-spotlight.directive';

/** Card de produto da loja (protótipo 02: .pcard de drones.html, acessorios.html e ofertas.html). */
@Component({
  selector: 'article[appPcard]',
  imports: [RouterLink, PrecoPipe],
  hostDirectives: [CardSpotlightDirective],
  host: { class: 'pcard' },
  template: `
    @let p = produto();
    <div class="top">
      <span class="lbl">{{ p.marca?.nome }}</span>
      @if (precoDe()) {
        <span class="chip">OFERTA</span>
      } @else if (!p.disponivel) {
        <span class="chip n">ESGOTADO</span>
      }
    </div>
    <a class="stage" [routerLink]="['/produto', p.slug]">
      @if (p.imagemPrincipal) {
        <img [src]="p.imagemPrincipal" [alt]="p.nome" loading="lazy" />
      }
    </a>
    <h3>
      <a [routerLink]="['/produto', p.slug]">{{ p.nome }}</a>
    </h3>
    <div class="pr" [class.sale]="!!precoDe()">
      @if (grupo()) {
        <span class="from">a partir de</span>
      }
      {{ p.precoMinimo | preco }}
      @if (precoDe(); as de) {
        <s>{{ de | preco }}</s>
      }
    </div>
    <div class="row2">
      <a class="btn" [class.grp]="grupo()" [routerLink]="['/produto', p.slug]">{{
        grupo() ? 'Escolher versão' : 'Ver detalhes'
      }}</a>
    </div>
  `,
})
export class Pcard {
  readonly produto = input.required<ProdutoLojaItem>();
  /** Preço cheio quando o card mostra uma oferta. */
  readonly precoDe = input<number | null>(null);
  /** O item representa um grupo de versões (D40). */
  readonly grupo = input(false);
}
