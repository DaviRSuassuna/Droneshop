import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ItemListaDesejos } from '../../../core/models/compra.model';
import { ContaService } from '../../../core/services/conta.service';
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
  private readonly conta = inject(ContaService);

  readonly itens = signal<ItemListaDesejos[]>([]);
  readonly carregado = signal(false);
  readonly comQueda = computed(() => this.itens().filter((i) => i.quedaDePreco).length);
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

  readonly aviso = signal<string | null>(null);

  /** Link público só de leitura (#86); copiado para a área de transferência. */
  compartilhar(): void {
    this.conta.compartilharFavoritos().subscribe((r) => {
      const link = `${location.origin}${r.url}`;
      void navigator.clipboard?.writeText(link);
      this.aviso.set(`Link copiado: ${link}`);
    });
  }

  moverTudo(): void {
    this.conta.moverFavoritosParaCarrinho().subscribe((r) => {
      const fora = r.naoAdicionados.length;
      this.aviso.set(
        fora
          ? `Itens disponíveis foram para o carrinho; ${fora} ficaram na lista (sem estoque ou indisponíveis).`
          : 'Tudo foi para o carrinho.',
      );
      this.carregar();
    });
  }

  queda(item: ItemListaDesejos): number {
    return (item.precoAoAdicionar ?? item.produto.preco) - item.produto.preco;
  }
}
