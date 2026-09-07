import { Component, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, NgFor } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CatalogService, Drone } from '../core/catalog.service';

@Component({
  imports: [CurrencyPipe, NgFor, RouterLink],
  template: `
    <section class="catalog wrap"><div class="catalog-head"><div><span class="eyebrow">Aeria / Catalogo</span><h1 class="display">Escolha seu<br><i>proximo voo.</i></h1></div><p class="muted">Equipamentos testados para criadores, pilotos e equipes que trabalham no ar.</p></div>
      <div class="toolbar"><div class="filters"><button [class.selected]="category() === 'Todos'" (click)="category.set('Todos')">Todos</button><button [class.selected]="category() === 'Profissionais'" (click)="category.set('Profissionais')">Profissionais</button><button [class.selected]="category() === 'Compactos'" (click)="category.set('Compactos')">Compactos</button></div><label class="sort">Ordenar <select (change)="sort.set($any($event.target).value)"><option value="featured">Destaques</option><option value="price">Menor preco</option><option value="name">Nome</option></select></label></div>
      <div class="catalog-grid"><a class="product-card" *ngFor="let drone of visibleProducts()" [routerLink]="['/produto', drone.id]"><div class="product-art"><span class="badge">{{ drone.destaque ?? 'Aeria' }}</span></div><div class="card-body"><div><h2>{{ drone.nome }}</h2><p>{{ drone.resumo }}</p></div><div class="specs"><span>{{ drone.autonomia }}</span><span>{{ drone.alcance }}</span><span>{{ drone.camera }}</span></div><strong>{{ drone.preco | currency:'BRL':'symbol':'1.0-0' }}</strong></div></a></div>
    </section>
  `,
  styles: [`
    .catalog { padding-top:78px; padding-bottom:100px; }.catalog-head { display:flex; align-items:end; justify-content:space-between; gap:30px; padding-bottom:70px; }.catalog-head h1 { margin:20px 0 0; }.catalog-head h1 i { color:var(--accent); font-style:normal; }.catalog-head p { max-width:270px; margin:0 0 4px; }.toolbar { display:flex; align-items:center; justify-content:space-between; border-top:1px solid var(--line); border-bottom:1px solid var(--line); padding:16px 0; margin-bottom:26px; }.filters { display:flex; gap:8px; }.filters button { border:1px solid transparent; background:none; color:var(--muted); padding:8px 12px; border-radius:999px; font-size:12px; }.filters button.selected,.filters button:hover { border-color:var(--line); color:var(--text); }.sort { display:flex; gap:8px; align-items:center; color:var(--muted); font-size:12px; }.sort select { border:0; color:var(--text); background:var(--bg); }.catalog-grid { display:grid; grid-template-columns:repeat(4,1fr); gap:14px; }.product-card { background:var(--surface); border:1px solid var(--line); transition:.2s ease; }.product-card:hover { transform:translateY(-4px); border-color:rgba(199,243,107,.5); }.product-card .product-art { min-height:220px; border-radius:0; }.badge { position:absolute; top:14px; left:14px; padding:5px 8px; background:var(--accent); color:var(--accent-ink); font:500 9px 'DM Mono',monospace; text-transform:uppercase; }.card-body { display:flex; flex-direction:column; gap:17px; padding:18px; }.card-body h2 { margin:0 0 8px; font:400 19px 'Space Grotesk',sans-serif; }.card-body p { margin:0; min-height:42px; color:var(--muted); font-size:12px; }.specs { display:flex; gap:9px; color:var(--muted); font:10px 'DM Mono',monospace; }.card-body strong { font:400 18px 'Space Grotesk',sans-serif; color:var(--accent); }.card-body strong::first-letter { color:var(--accent); }
    @media (max-width:900px) { .catalog-grid { grid-template-columns:repeat(2,1fr); } } @media (max-width:600px) { .catalog { padding-top:48px; }.catalog-head { display:block; padding-bottom:42px; }.catalog-head p { margin-top:24px; }.toolbar { display:block; }.sort { margin-top:14px; }.catalog-grid { grid-template-columns:1fr; } }
  `],
})
export class CatalogComponent {
  private readonly catalog = inject(CatalogService);
  readonly category = signal('Todos');
  readonly sort = signal('featured');
  readonly products = signal<Drone[]>([]);
  readonly visibleProducts = computed(() => {
    const category = this.category();
    const products = this.products().filter((drone) => category === 'Todos' || drone.categoria === category);
    return [...products].sort((left, right) => this.sort() === 'price' ? left.preco - right.preco : this.sort() === 'name' ? left.nome.localeCompare(right.nome) : 0);
  });

  constructor() {
    this.catalog.list().subscribe((products) => this.products.set(products));
  }
}