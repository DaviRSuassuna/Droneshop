import {
  DOCUMENT,
  Component,
  DestroyRef,
  ElementRef,
  effect,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NgOptimizedImage } from '@angular/common';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { catchError, filter, of } from 'rxjs';
import { ContaFolha } from '../account-menu/conta-folha';
import { LojaService } from '../../../core/services/loja.service';
import { MarcaLoja } from '../../../core/models/loja.model';
import { CarrinhoService } from '../../../core/services/carrinho.service';
import { AuthService } from '../../../core/services/auth.service';

/** Depois desta altura o menu encolhe (protótipo: navbar-aceternity.js, bloco B). */
export const ALTURA_ENCOLHE = 40;
/** Só some ao descer depois desta altura. */
export const ALTURA_SOME = 200;
/** Folga de rolagem para não piscar com o trackpad. */
const FOLGA = 4;
/** Atraso para fechar o mega menu, para o mouse passar do link para o painel. */
export const ATRASO_FECHA_MEGA = 180;
/** Abaixo desta largura o ícone da conta abre a folha em vez de ir para o perfil. */
const CONSULTA_CELULAR = '(max-width: 900px)';

/**
 * Menu da loja (protótipo 02, index.html + navbar-aceternity.js): some ao descer, volta ao
 * subir e encolhe depois do topo; mega menu de Drones com as subcategorias de drone e as marcas
 * em destaque da API (/loja/categorias e /loja/marcas, #21); busca que leva para /drones?q=.
 * As sugestões da busca ficam para a #43. O contador da sacola vem do CarrinhoService (#24) e
 * pula (.sp-bump) quando o número muda.
 */
@Component({
  selector: 'app-storefront-nav',
  imports: [RouterLink, RouterLinkActive, NgOptimizedImage, ContaFolha],
  templateUrl: './storefront-nav.html',
  host: {
    '(window:scroll)': 'aoRolar()',
    '(document:keydown)': 'aoTeclar($event)',
  },
})
export class StorefrontNav {
  private readonly router = inject(Router);
  private readonly doc = inject(DOCUMENT);
  private readonly folha = viewChild.required(ContaFolha);
  private readonly campoBusca = viewChild.required<ElementRef<HTMLInputElement>>('buscaDesktop');

  readonly menuAberto = signal(false);
  readonly buscaAberta = signal(false);
  readonly megaAberto = signal(false);
  readonly encolhido = signal(false);
  readonly escondido = signal(false);
  /** "Por uso": subcategorias das categorias de drone do menu (D39, D43). */
  readonly usos = signal<{ id: number; nome: string; slug: string; categoria: string }[]>([]);
  readonly marcas = signal<MarcaLoja[]>([]);
  /** Itens na sacola (#24). */
  readonly sacola = inject(CarrinhoService).quantidade;
  readonly pulaSacola = signal(false);

  private ultimaRolagem = 0;
  private timerMega: ReturnType<typeof setTimeout> | undefined;
  private timerSacola: ReturnType<typeof setTimeout> | undefined;

  constructor() {
    this.ultimaRolagem = this.rolagemAtual();
    this.encolhido.set(this.ultimaRolagem > ALTURA_ENCOLHE);
    this.router.events
      .pipe(
        filter((e) => e instanceof NavigationEnd),
        takeUntilDestroyed(),
      )
      .subscribe(() => this.fecharTudo());
    inject(DestroyRef).onDestroy(() => {
      clearTimeout(this.timerMega);
      clearTimeout(this.timerSacola);
    });
    this.carregarSacola();
    let anterior = this.sacola();
    effect(() => {
      const atual = this.sacola();
      if (atual === anterior) return;
      anterior = atual;
      this.pulaSacola.set(false);
      clearTimeout(this.timerSacola);
      setTimeout(() => this.pulaSacola.set(true));
      this.timerSacola = setTimeout(() => this.pulaSacola.set(false), 600);
    });
    const loja = inject(LojaService);
    loja
      .listarCategorias('DRONE')
      .subscribe((categorias) =>
        this.usos.set(
          categorias.flatMap((c) => c.subcategorias.map((s) => ({ ...s, categoria: c.nome }))),
        ),
      );
    loja
      .listarMarcas(true)
      .pipe(catchError(() => of([])))
      .subscribe((m) => this.marcas.set(m.slice(0, 4)));
  }

  /** Número da sacola ao abrir a loja: carrinho do cliente logado ou o anônimo do token. */
  private carregarSacola(): void {
    const carrinho = inject(CarrinhoService);
    const req = inject(AuthService).autenticado()
      ? carrinho.doCliente()
      : carrinho.token()
        ? carrinho.buscar()
        : null;
    req?.pipe(catchError(() => of(null))).subscribe();
  }

  aoRolar(): void {
    const y = this.rolagemAtual();
    this.encolhido.set(y > ALTURA_ENCOLHE);
    const algoAberto = this.menuAberto() || this.buscaAberta() || this.megaAberto();
    if (!algoAberto && y > ALTURA_SOME && y > this.ultimaRolagem + FOLGA) this.escondido.set(true);
    else if (y < this.ultimaRolagem - FOLGA || algoAberto) this.escondido.set(false);
    this.ultimaRolagem = y;
  }

  aoTeclar(e: KeyboardEvent): void {
    if (e.key === 'Escape') {
      this.megaAberto.set(false);
      return;
    }
    // "/" leva o foco para a busca, como no protótipo (fora de campos de texto)
    const ativo = this.doc.activeElement;
    if (e.key !== '/' || (ativo && /^(input|textarea|select)$/i.test(ativo.tagName))) return;
    const campo = this.campoBusca().nativeElement;
    if (campo.offsetParent) {
      e.preventDefault();
      campo.focus();
    }
  }

  alternarMenu(): void {
    this.menuAberto.update((v) => !v);
    if (this.menuAberto()) this.buscaAberta.set(false);
  }

  alternarBusca(campo: HTMLInputElement): void {
    this.buscaAberta.update((v) => !v);
    if (this.buscaAberta()) {
      this.menuAberto.set(false);
      setTimeout(() => campo.focus());
    }
  }

  buscar(e: Event, termo: string): void {
    e.preventDefault();
    const q = termo.trim();
    // com termo vai para a busca (#43), que grava o termo; vazio abre os drones
    if (q) this.router.navigate(['/busca'], { queryParams: { q } });
    else this.router.navigate(['/drones']);
  }

  abrirMega(): void {
    clearTimeout(this.timerMega);
    this.megaAberto.set(true);
  }

  fecharMega(): void {
    clearTimeout(this.timerMega);
    this.timerMega = setTimeout(() => this.megaAberto.set(false), ATRASO_FECHA_MEGA);
  }

  aoSairFocoMega(e: FocusEvent, painel: HTMLElement): void {
    if (!painel.contains(e.relatedTarget as Node | null)) this.fecharMega();
  }

  /** No celular o ícone da conta abre a folha; no desktop segue para o perfil. */
  abrirConta(e: MouseEvent): void {
    if (!this.doc.defaultView?.matchMedia?.(CONSULTA_CELULAR).matches) return;
    e.preventDefault();
    this.folha().abrir();
  }

  sairDaConta(): void {
    this.router.navigateByUrl('/entrar');
  }

  private fecharTudo(): void {
    this.menuAberto.set(false);
    this.buscaAberta.set(false);
    clearTimeout(this.timerMega);
    this.megaAberto.set(false);
  }

  private rolagemAtual(): number {
    return this.doc.defaultView?.scrollY ?? 0;
  }
}
