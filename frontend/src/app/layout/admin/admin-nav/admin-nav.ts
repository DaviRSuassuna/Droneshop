import {
  Component,
  ElementRef,
  Injector,
  afterNextRender,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter, map } from 'rxjs';

export interface LinkAdmin {
  rotulo: string;
  rota: string;
  descricao?: string;
  /** Prefixos de rota que também marcam este link como atual. */
  prefixos?: string[];
  /** Só marca como atual na rota exata. */
  exata?: boolean;
}

interface GrupoAdmin {
  rotulo: string;
  colunas: { titulo: string; links: LinkAdmin[] }[];
  rapidas: LinkAdmin[];
}

// Barra do painel no protótipo 02 (admin-fx.js, bloco K): flutuante, com Dashboard e três
// grupos que abrem um menu (Catálogo, Vendas, Administração). No celular (.mnav) os links
// continuam soltos e roláveis, como no protótipo.
@Component({
  selector: 'app-admin-nav',
  imports: [RouterLink],
  templateUrl: './admin-nav.html',
  host: {
    '(document:keydown.escape)': 'fechar()',
    '(document:click)': 'cliqueFora($event)',
  },
})
export class AdminNav {
  readonly dashboard: LinkAdmin = { rotulo: 'Dashboard', rota: '/admin', exata: true };

  readonly grupos: GrupoAdmin[] = [
    {
      rotulo: 'Catálogo',
      colunas: [
        {
          titulo: 'Produtos',
          links: [
            {
              rotulo: 'Produtos',
              rota: '/admin/produtos',
              descricao: 'Drones, acessórios e kits',
              prefixos: ['/admin/produtos', '/admin/drones', '/admin/acessorios', '/admin/kits'],
            },
            {
              rotulo: 'Grupos de produto',
              rota: '/admin/grupos',
              descricao: 'Versões do mesmo modelo juntas',
            },
            { rotulo: 'Marcas', rota: '/admin/marcas', descricao: 'Fabricantes e país de origem' },
          ],
        },
        {
          titulo: 'Organização',
          links: [
            {
              rotulo: 'Categorias',
              rota: '/admin/categorias',
              descricao: 'Menu e ordem de exibição',
            },
            {
              rotulo: 'Subcategorias',
              rota: '/admin/subcategorias',
              descricao: 'Dentro de cada categoria',
            },
          ],
        },
      ],
      rapidas: [
        { rotulo: 'Novo drone', rota: '/admin/drones/novo' },
        { rotulo: 'Novo acessório', rota: '/admin/acessorios/novo' },
        { rotulo: 'Novo kit', rota: '/admin/kits/novo' },
        { rotulo: 'Novo grupo de produto', rota: '/admin/grupos/novo' },
      ],
    },
    {
      rotulo: 'Vendas',
      colunas: [
        {
          titulo: 'Vendas',
          links: [
            {
              rotulo: 'Pedidos',
              rota: '/admin/pedidos',
              descricao: 'Pagamento, separação e entrega',
            },
          ],
        },
        {
          titulo: 'Ofertas',
          links: [
            {
              rotulo: 'Promoções',
              rota: '/admin/promocoes',
              descricao: 'Descontos por período e produto',
            },
            {
              rotulo: 'Cupons',
              rota: '/admin/cupons',
              descricao: 'Códigos de desconto, como PILOTO10',
            },
          ],
        },
        {
          titulo: 'Compras',
          links: [
            {
              rotulo: 'Fornecedores',
              rota: '/admin/fornecedores',
              descricao: 'CNPJ, contato e endereço',
            },
          ],
        },
      ],
      rapidas: [
        { rotulo: 'Nova promoção', rota: '/admin/promocoes/nova' },
        { rotulo: 'Novo cupom', rota: '/admin/cupons/novo' },
        { rotulo: 'Novo fornecedor', rota: '/admin/fornecedores/novo' },
      ],
    },
    {
      rotulo: 'Administração',
      colunas: [
        {
          titulo: 'Operações',
          links: [
            {
              rotulo: 'Posição de estoque',
              rota: '/admin/estoque',
              descricao: 'Disponível e mínimo por produto',
              exata: true,
            },
            {
              rotulo: 'Movimentações',
              rota: '/admin/estoque/movimento',
              descricao: 'Entradas, saídas e ajustes',
            },
          ],
        },
        {
          titulo: 'Administração',
          links: [
            {
              rotulo: 'Usuários',
              rota: '/admin/usuarios',
              descricao: 'Equipe com acesso ao painel',
            },
            {
              rotulo: 'Configuração da loja',
              rota: '/admin/configuracao',
              descricao: 'Parâmetros gerais',
            },
            {
              rotulo: 'E-mails',
              rota: '/admin/templates-email',
              descricao: 'Templates e envios',
            },
          ],
        },
      ],
      rapidas: [
        { rotulo: 'Novo movimento', rota: '/admin/estoque/movimento' },
        { rotulo: 'Novo usuário administrativo', rota: '/admin/usuarios/novo' },
      ],
    },
  ];

  /** Links soltos da faixa do celular (.mnav), na ordem dos grupos. */
  readonly itensCelular: LinkAdmin[] = [
    this.dashboard,
    ...this.grupos.flatMap((g) => g.colunas.flatMap((c) => c.links)),
  ];

  /** Índice do grupo com o menu aberto, ou null. */
  readonly aberto = signal<number | null>(null);

  private readonly router = inject(Router);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private readonly url = toSignal(
    this.router.events.pipe(
      filter((e) => e instanceof NavigationEnd),
      map(() => this.router.url),
    ),
    { initialValue: this.router.url },
  );
  private timer: ReturnType<typeof setTimeout> | undefined;

  /** Grupo que contém a tela atual: o botão dele fica marcado. */
  readonly grupoAtual = computed(() =>
    this.grupos.findIndex((g) => g.colunas.some((c) => c.links.some((l) => this.atual(l)))),
  );

  constructor() {
    // A aba atual fica centralizada quando a faixa do celular rola para o lado.
    const injector = inject(Injector);
    const centralizar = () =>
      this.host.querySelectorAll<HTMLElement>('.mnav a[aria-current]').forEach((a) => {
        const menu = a.parentElement;
        if (menu) menu.scrollLeft = a.offsetLeft - (menu.clientWidth - a.offsetWidth) / 2;
      });
    afterNextRender(centralizar);
    this.router.events
      .pipe(
        filter((e) => e instanceof NavigationEnd),
        takeUntilDestroyed(),
      )
      .subscribe(() => {
        this.fechar();
        afterNextRender(centralizar, { injector });
      });
  }

  atual(link: LinkAdmin): boolean {
    const url = this.url().split(/[?#]/)[0];
    if (url === link.rota) return true;
    if (link.exata) return false;
    return (link.prefixos ?? [link.rota]).some((p) => url === p || url.startsWith(`${p}/`));
  }

  abrir(i: number): void {
    clearTimeout(this.timer);
    this.aberto.set(i);
  }

  sairDepois(): void {
    clearTimeout(this.timer);
    this.timer = setTimeout(() => this.fechar(), 200);
  }

  alternar(i: number): void {
    if (this.aberto() === i) this.fechar();
    else this.abrir(i);
  }

  fechar(): void {
    clearTimeout(this.timer);
    this.aberto.set(null);
  }

  cliqueFora(e: Event): void {
    if (!this.host.contains(e.target as Node)) this.fechar();
  }
}
