import { Component, computed, effect, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { map } from 'rxjs';
import {
  CategoriaLoja,
  FiltroProdutosLoja,
  FiltrosLoja,
  ProdutoLojaItem,
} from '../../../core/models/loja.model';
import { LojaService } from '../../../core/services/loja.service';
import { Pcard } from '../pcard/pcard';

type Tipo = 'DRONE' | 'ACESSORIO';

interface Faixa {
  rotulo: string;
  valor: number;
}

const POR_PAGINA = 9;

/**
 * Drones e Acessórios (protótipo 02: drones.html e acessorios.html). As páginas filtram por
 * tipo, não por categoria (D39); o grupo "Uso"/"Tipo" lista as subcategorias de /loja/categorias.
 * Filtros e página ficam na URL (?marca=&subcategoria=&autonomiaMin=&alcanceMin=&q=&ordem=&page=).
 */
@Component({
  selector: 'app-catalogo-listagem',
  imports: [RouterLink, Pcard],
  templateUrl: './catalogo-listagem.html',
})
export class CatalogoListagem {
  private readonly loja = inject(LojaService);
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly tipo: Tipo = this.rota.snapshot.data['tipo'] ?? 'DRONE';
  readonly titulo = this.tipo === 'DRONE' ? 'Drones' : 'Acessórios';
  readonly rotuloGrupo = this.tipo === 'DRONE' ? 'Uso' : 'Tipo';
  readonly nota =
    this.tipo === 'DRONE'
      ? 'Todos os drones têm 12 meses de garantia e orientação para o registro SISANT.'
      : 'Todos os acessórios são testados pela Aeria e têm garantia de 12 meses.';

  readonly alcances: Faixa[] = [
    { rotulo: 'A partir de 5 km', valor: 5000 },
    { rotulo: 'A partir de 10 km', valor: 10000 },
    { rotulo: 'A partir de 15 km', valor: 15000 },
  ];
  readonly autonomias: Faixa[] = [
    { rotulo: 'A partir de 20 min', valor: 20 },
    { rotulo: 'A partir de 35 min', valor: 35 },
  ];

  private readonly query = toSignal(this.rota.queryParamMap.pipe(map((q) => q)), {
    initialValue: this.rota.snapshot.queryParamMap,
  });

  readonly filtro = computed<FiltroProdutosLoja>(() => {
    const q = this.query();
    const numero = (k: string) => (q.get(k) ? Number(q.get(k)) : null);
    return {
      tipo: this.tipo,
      marca: q.get('marca') ?? undefined,
      subcategoria: q.get('subcategoria') ?? undefined,
      nome: q.get('q') ?? undefined,
      autonomiaMin: numero('autonomiaMin'),
      alcanceMin: numero('alcanceMin'),
      precoMin: numero('precoMin'),
      precoMax: numero('precoMax'),
      ordem: (q.get('ordem') as FiltroProdutosLoja['ordem']) ?? '',
    };
  });
  readonly pagina = computed(() => Number(this.query().get('page') ?? 0));

  readonly produtos = signal<ProdutoLojaItem[]>([]);
  readonly total = signal(0);
  readonly totalPaginas = signal(0);
  readonly facetas = signal<FiltrosLoja>({ marcas: [], preco: { min: null, max: null } });
  readonly categorias = signal<CategoriaLoja[]>([]);
  readonly filtrosAbertos = signal(false);

  readonly subcategorias = computed(() => this.categorias().flatMap((c) => c.subcategorias));
  readonly paginas = computed(() => Array.from({ length: this.totalPaginas() }, (_, i) => i));
  readonly exibindo = computed(() => {
    const inicio = this.pagina() * POR_PAGINA;
    return this.total()
      ? `Exibindo ${inicio + 1} a ${inicio + this.produtos().length} de ${this.total()}`
      : 'Nenhum produto encontrado';
  });

  /** Etiquetas dos filtros ativos, com o parâmetro que o "×" tira. */
  readonly etiquetas = computed(() => {
    const f = this.filtro();
    const tags: { texto: string; chave: string }[] = [];
    if (f.marca)
      tags.push({
        texto: `Marca: ${this.facetas().marcas.find((m) => m.slug === f.marca)?.nome ?? f.marca}`,
        chave: 'marca',
      });
    if (f.subcategoria)
      tags.push({
        texto: `${this.rotuloGrupo}: ${this.subcategorias().find((s) => s.slug === f.subcategoria)?.nome ?? f.subcategoria}`,
        chave: 'subcategoria',
      });
    if (f.alcanceMin)
      tags.push({ texto: `A partir de ${f.alcanceMin / 1000} km`, chave: 'alcanceMin' });
    if (f.autonomiaMin)
      tags.push({ texto: `A partir de ${f.autonomiaMin} min`, chave: 'autonomiaMin' });
    if (f.nome) tags.push({ texto: `Busca: ${f.nome}`, chave: 'q' });
    return tags;
  });

  constructor() {
    this.loja.listarCategorias(this.tipo).subscribe((c) => this.categorias.set(c));
    this.loja.buscarFiltros(undefined, this.tipo).subscribe((f) => this.facetas.set(f));
    effect(() => {
      this.loja.listarProdutos(this.filtro(), this.pagina(), POR_PAGINA).subscribe((p) => {
        this.produtos.set(p.items);
        this.total.set(p.totalItems);
        this.totalPaginas.set(p.totalPages);
      });
    });
  }

  marcado(chave: string, valor: string | number): boolean {
    return this.query().get(chave) === `${valor}`;
  }

  /** Cada grupo de filtro aceita um valor: marcar troca, desmarcar tira. */
  alternar(chave: string, valor: string | number): void {
    this.aplicar({ [chave]: this.marcado(chave, valor) ? null : valor });
  }

  tirar(chave: string): void {
    this.aplicar({ [chave]: null });
  }

  preco(chave: 'precoMin' | 'precoMax', texto: string): void {
    const valor = Number(texto.replace(/\D/g, ''));
    this.aplicar({ [chave]: valor || null });
  }

  ordenar(ordem: string): void {
    this.aplicar({ ordem: ordem || null });
  }

  limpar(e: Event): void {
    e.preventDefault();
    this.router.navigate([], { relativeTo: this.rota, queryParams: {} });
  }

  irPara(e: Event, page: number): void {
    e.preventDefault();
    if (page < 0 || page >= this.totalPaginas()) return;
    this.router.navigate([], {
      relativeTo: this.rota,
      queryParams: { page: page || null },
      queryParamsHandling: 'merge',
    });
  }

  private aplicar(params: Record<string, string | number | null>): void {
    this.router.navigate([], {
      relativeTo: this.rota,
      queryParams: { ...params, page: null },
      queryParamsHandling: 'merge',
    });
  }
}
