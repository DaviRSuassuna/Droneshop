import { CurrencyPipe, UpperCasePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CatalogService, Drone } from '../core/catalog.service';

@Component({
  imports: [CurrencyPipe, RouterLink, UpperCasePipe],
  template: `
    @if (product(); as drone) {
      <main class="product-page wrap"><a class="back" routerLink="/drones">← Voltar ao catalogo</a><div class="product-grid"><div class="product-art product-visual"><span class="visual-code">AERIA / {{ drone.id | uppercase }}</span></div><div class="product-info"><span class="eyebrow">{{ drone.categoria }} / {{ drone.destaque }}</span><h1>{{ drone.nome }}</h1><p class="summary">{{ drone.resumo }}</p><div class="price">{{ drone.preco | currency:'BRL':'symbol':'1.0-0' }} <small>ou 12x sem juros</small></div><div class="purchase"><label>Quantidade <input type="number" min="1" value="1"></label><button class="button primary" type="button">Adicionar ao carrinho</button></div><div class="spec-list"><div><span>Autonomia</span><b>{{ drone.autonomia }}</b></div><div><span>Transmissao</span><b>{{ drone.alcance }}</b></div><div><span>Camera</span><b>{{ drone.camera }}</b></div><div><span>Disponibilidade</span><b>{{ drone.estoque }} unidades</b></div></div></div></div></main>
    } @else { <div class="loading wrap">Carregando produto...</div> }
  `,
  styles: [`
    .product-page { padding-top:45px; padding-bottom:110px; }.back { display:inline-block; margin-bottom:35px; color:var(--muted); font-size:12px; }.back:hover { color:var(--accent); }.product-grid { display:grid; grid-template-columns:1.1fr .9fr; gap:70px; align-items:start; }.product-visual { min-height:590px; }.visual-code { position:absolute; top:25px; left:25px; color:var(--muted); font:10px 'DM Mono',monospace; letter-spacing:.1em; }.product-info { padding-top:22px; }.product-info h1 { margin:22px 0 15px; font:300 clamp(44px,5vw,72px)/.95 'Space Grotesk',sans-serif; letter-spacing:-.05em; }.summary { max-width:370px; color:var(--muted); font-size:15px; }.price { margin:38px 0 24px; color:var(--accent); font:400 29px 'Space Grotesk',sans-serif; }.price small { display:block; margin-top:5px; color:var(--muted); font:11px 'DM Mono',monospace; }.purchase { display:flex; gap:10px; align-items:end; }.purchase label { display:flex; flex-direction:column; gap:7px; color:var(--muted); font-size:11px; }.purchase input { width:75px; height:44px; border:1px solid var(--line); color:var(--text); background:var(--surface); padding:0 12px; }.purchase .button { flex:1; }.spec-list { margin-top:47px; border-top:1px solid var(--line); }.spec-list div { display:flex; justify-content:space-between; gap:20px; padding:14px 0; border-bottom:1px solid var(--line); font-size:12px; }.spec-list span { color:var(--muted); }.spec-list b { font-weight:500; }.loading { padding:100px 0; color:var(--muted); }
    @media (max-width:800px) { .product-grid { grid-template-columns:1fr; gap:28px; }.product-visual { min-height:360px; }.product-info { padding-top:0; } }
  `],
})
export class ProductComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly catalog = inject(CatalogService);
  readonly product = signal<Drone | null>(null);

  constructor() {
    this.route.paramMap.subscribe((params) => {
      this.catalog.find(params.get('id') ?? '').subscribe((product) => this.product.set(product));
    });
  }
}