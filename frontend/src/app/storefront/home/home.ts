import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CategoriaLoja, ProdutoLojaItem } from '../../core/models/loja.model';
import { LojaService } from '../../core/services/loja.service';
import { BlurRevealDirective } from '../../shared/directives/blur-reveal.directive';
import { CardSpotlightDirective } from '../../shared/directives/card-spotlight.directive';
import { SpotlightDirective } from '../../shared/directives/spotlight.directive';
import { PrecoPipe } from '../catalogo/preco.pipe';

interface Depoimento {
  nome: string;
  perfil: string;
  texto: string;
}

/**
 * Capa da loja (protótipo 02: index.html). A capa do Falcon X2 Pro é o lançamento fixo do
 * protótipo; a vitrine, o comparativo e "O que você vai voar?" vêm da API (/loja/produtos e
 * /loja/categorias). Efeitos só da capa: luz que segue o mouse, raios, drone em voo, borda
 * girando no botão principal e depoimentos rolando (styles/efeitos).
 */
@Component({
  selector: 'app-home',
  imports: [RouterLink, PrecoPipe, BlurRevealDirective, CardSpotlightDirective, SpotlightDirective],
  templateUrl: './home.html',
})
export class Home {
  private readonly loja = inject(LojaService);

  readonly destaques = signal<ProdutoLojaItem[]>([]);
  readonly categorias = signal<CategoriaLoja[]>([]);
  readonly comparar = computed(() => this.destaques().slice(0, 3));

  /** Depoimentos do protótipo (bloco J); viram dado da API quando houver avaliações. */
  readonly depoimentos: Depoimento[] = [
    {
      nome: 'Rafael M.',
      perfil: 'Videomaker · Palmas',
      texto:
        'O Falcon chegou em 2 dias e a imagem em 4K 60 é absurda. Atendimento me ajudou a escolher o kit certo.',
    },
    {
      nome: 'Juliana S.',
      perfil: 'Agrônoma',
      texto:
        'Usamos o Heron com RTK no mapeamento das fazendas. Precisão de centímetro, como prometido.',
    },
    {
      nome: 'Pedro A.',
      perfil: 'Iniciante',
      texto:
        'Comecei pelo Nimbus Mini, que não precisa de registro. Leve, estável e fácil de pilotar.',
    },
    {
      nome: 'Carla R.',
      perfil: 'Fotógrafa de casamentos',
      texto: 'O gimbal segura até com vento. Paguei no Pix com desconto e veio tudo certinho.',
    },
    {
      nome: 'Lucas T.',
      perfil: 'Piloto FPV',
      texto: 'O Vortex é rápido demais. Peças de reposição chegam rápido, isso faz diferença.',
    },
    {
      nome: 'Marina R.',
      perfil: 'Corretora de imóveis',
      texto: 'Fotos aéreas dos imóveis venderam mais rápido. Suporte respondeu no mesmo dia.',
    },
  ];
  readonly depoimentosInvertidos = [...this.depoimentos].reverse();

  constructor() {
    this.loja
      .listarProdutos({ tipo: 'DRONE', ordem: 'lancamento' }, 0, 8)
      .subscribe((p) => this.destaques.set(p.items));
    this.loja.listarCategorias('DRONE').subscribe((c) => this.categorias.set(c));
  }

  iniciais(nome: string): string {
    return nome
      .split(' ')
      .map((p) => p[0])
      .join('');
  }

  /** Setas da vitrine: passam um card por vez. */
  rolar(trilho: HTMLElement, sentido: number): void {
    const card = trilho.firstElementChild as HTMLElement | null;
    const passo = card ? card.getBoundingClientRect().width + 12 : trilho.clientWidth;
    trilho.scrollBy({ left: sentido * passo, behavior: 'smooth' });
  }
}
