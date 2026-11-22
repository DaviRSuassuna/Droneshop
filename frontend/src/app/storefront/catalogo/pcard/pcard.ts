import { Component, inject, input, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { Observable, of, switchMap } from 'rxjs';
import { CarrinhoService } from '../../../core/services/carrinho.service';
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
    @let de = precoDe() ?? p.precoDe ?? null;
    @let versoes = p.versoes ?? [];
    @let emGrupo = grupo() || versoes.length > 1;
    <div class="top">
      <span class="lbl">{{ p.marca?.nome }}</span>
      @if (de) {
        <span class="chip">OFERTA</span>
      } @else if (!p.disponivel) {
        <span class="chip n">ESGOTADO</span>
      } @else if (versoes.length > 1) {
        <span class="lbl">{{ versoes.length }} VERSÕES</span>
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
    @if (versoes.length > 1) {
      <div class="specs">{{ versoes.join(' · ') }}</div>
    } @else if (p.resumo) {
      <div class="specs">{{ p.resumo }}</div>
    }
    <div class="pr" [class.sale]="!!de">
      @if (emGrupo) {
        <span class="from">a partir de</span>
      }
      {{ p.precoMinimo | preco }}
      @if (de) {
        <s>{{ de | preco }}</s>
      }
      @if (versoes.length > 1 && p.resumo) {
        <small>{{ p.resumo }}</small>
      }
    </div>
    <div class="row2">
      <a class="btn" [class.grp]="emGrupo" [routerLink]="['/produto', p.slug]">{{
        emGrupo ? 'Escolher versão' : 'Ver detalhes'
      }}</a>
      @if (!emGrupo && p.disponivel) {
        <button
          type="button"
          class="btn sq"
          aria-label="Adicionar ao carrinho"
          [disabled]="adicionando()"
          (click)="adicionar(p.id, p.slug)"
        >
          <svg class="ico" aria-hidden="true"><use href="#i-bag" /></svg>
        </button>
      }
    </div>
  `,
})
export class Pcard {
  readonly produto = input.required<ProdutoLojaItem>();
  /** Preço cheio quando o card mostra uma oferta. */
  readonly precoDe = input<number | null>(null);
  /** O item representa um grupo de versões (D40). */
  readonly grupo = input(false);

  private readonly carrinho = inject(CarrinhoService);
  private readonly router = inject(Router);
  readonly adicionando = signal(false);

  /** Botão de sacola do protótipo: põe 1 unidade no carrinho; se não der, abre a página do produto. */
  adicionar(idProduto: number, slug: string): void {
    if (this.adicionando()) return;
    this.adicionando.set(true);
    const inicio: Observable<unknown> = this.carrinho.token() ? of(null) : this.carrinho.criar();
    inicio.pipe(switchMap(() => this.carrinho.adicionar(idProduto, 1))).subscribe({
      next: () => void this.router.navigate(['/carrinho']),
      error: () => {
        this.adicionando.set(false);
        void this.router.navigate(['/produto', slug]);
      },
    });
  }
}
