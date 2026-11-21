import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { OfertaLoja, ProdutoLojaItem } from '../../../core/models/loja.model';
import { LojaService } from '../../../core/services/loja.service';
import { BlurRevealDirective } from '../../../shared/directives/blur-reveal.directive';
import { Pcard } from '../pcard/pcard';
import { PrecoPipe } from '../preco.pipe';

const DIA = 86_400_000;

/**
 * Ofertas (protótipo 02: ofertas.html): promoções vigentes de produto e kits marcados para
 * Ofertas (/loja/ofertas). A oferta em destaque é a de maior desconto; o contador vai até o
 * fim da promoção que acaba primeiro.
 */
@Component({
  selector: 'app-ofertas',
  imports: [RouterLink, Pcard, PrecoPipe, BlurRevealDirective],
  templateUrl: './ofertas.html',
  styleUrl: './ofertas.scss',
})
export class Ofertas {
  readonly ofertas = signal<OfertaLoja[]>([]);
  private readonly agora = Date.now();

  readonly desconto = (o: OfertaLoja) =>
    o.precoDe > 0 ? Math.round((1 - o.precoPor / o.precoDe) * 100) : 0;

  readonly destaque = computed(
    () => [...this.ofertas()].sort((a, b) => this.desconto(b) - this.desconto(a))[0] ?? null,
  );
  readonly descontoMaximo = computed(() => (this.destaque() ? this.desconto(this.destaque()!) : 0));
  readonly fim = computed(() => {
    const datas = this.ofertas()
      .map((o) => o.promocao?.dataFim)
      .filter((d): d is string => !!d)
      .map((d) => new Date(d).getTime());
    return datas.length ? Math.min(...datas) : null;
  });
  readonly restante = computed(() => {
    const fim = this.fim();
    const ms = fim ? Math.max(fim - this.agora, 0) : 0;
    const dois = (n: number) => `${n}`.padStart(2, '0');
    return {
      dias: dois(Math.floor(ms / DIA)),
      horas: dois(Math.floor((ms % DIA) / 3_600_000)),
      min: dois(Math.floor((ms % 3_600_000) / 60_000)),
    };
  });

  constructor() {
    inject(LojaService)
      .listarOfertas()
      .subscribe((o) => this.ofertas.set(o));
  }

  card(o: OfertaLoja): ProdutoLojaItem {
    return {
      id: o.produto.id,
      nome: o.produto.nome,
      slug: o.produto.slug,
      tipo: { id: 0, nome: '' },
      marca: null,
      precoMinimo: o.precoPor,
      imagemPrincipal: null,
      disponivel: true,
    };
  }
}
