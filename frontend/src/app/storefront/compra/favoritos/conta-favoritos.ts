import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ItemListaDesejos } from '../../../core/models/compra.model';
import { ListaDesejosService } from '../../../core/services/lista-desejos.service';
import { reais } from '../../../core/utils/moeda';
import { AccountMenu } from '../../../layout/storefront/account-menu/account-menu';
import { ContaSessao } from '../../conta/conta-sessao';

/** "Minha conta > Favoritos" (protótipo 02, conta-favoritos.html; #22). */
@Component({
  selector: 'app-conta-favoritos',
  imports: [AccountMenu, RouterLink, DatePipe],
  providers: [ContaSessao],
  templateUrl: './conta-favoritos.html',
  styleUrl: './conta-favoritos.scss',
})
export class ContaFavoritos implements OnInit {
  protected readonly sessao = inject(ContaSessao);
  private readonly desejos = inject(ListaDesejosService);

  readonly itens = signal<ItemListaDesejos[]>([]);
  readonly carregado = signal(false);
  readonly comQueda = computed(() => this.itens().filter((i) => i.caiuPreco).length);
  protected readonly reais = reais;

  ngOnInit(): void {
    this.sessao.carregar();
    this.carregar();
  }

  carregar(): void {
    this.desejos.listar().subscribe((itens) => {
      this.itens.set(itens);
      this.carregado.set(true);
    });
  }

  remover(item: ItemListaDesejos): void {
    this.desejos
      .remover(item.produto.id)
      .subscribe(() =>
        this.itens.update((lista) => lista.filter((i) => i.produto.id !== item.produto.id)),
      );
  }

  queda(item: ItemListaDesejos): number {
    return (item.precoAoAdicionar ?? item.produto.preco) - item.produto.preco;
  }
}
