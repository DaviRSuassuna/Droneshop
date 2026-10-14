import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AdminNav } from '../../../../layout/admin/admin-nav/admin-nav';
import { SubcategoriaService } from '../../../../core/services/subcategoria.service';
import { CategoriaService } from '../../../../core/services/categoria.service';
import { Subcategoria } from '../../../../core/models/subcategoria.model';
import { Categoria } from '../../../../core/models/categoria.model';
import { STATUS_PUBLICACAO, StatusRef } from '../../../../core/models/status.model';

const TAMANHO_PAGINA = 10;

@Component({
  selector: 'app-subcategorias-listagem',
  standalone: true,
  imports: [RouterLink, AdminNav],
  templateUrl: './subcategorias-listagem.html',
})
export class SubcategoriasListagem implements OnInit {
  private readonly subcategoriaService = inject(SubcategoriaService);
  private readonly categoriaService = inject(CategoriaService);

  readonly grid = 'minmax(0, 1fr) 200px 90px 118px 64px';

  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly subcategorias = signal<Subcategoria[]>([]);
  readonly categorias = signal<Categoria[]>([]);

  readonly termoBusca = signal('');
  readonly filtroCategoria = signal<number | null>(null);
  readonly STATUS = STATUS_PUBLICACAO;
  readonly filtroStatus = signal<number | null>(null);
  readonly pagina = signal(0);

  readonly subcategoriasFiltradas = computed(() => {
    const termo = this.termoBusca().trim().toLowerCase();
    const categoria = this.filtroCategoria();
    const status = this.filtroStatus();
    return this.subcategorias().filter(
      (s) =>
        (!termo || s.nome.toLowerCase().includes(termo) || s.slug.includes(termo)) &&
        (!categoria || s.categoria.id === categoria) &&
        (!status || s.status.id === status),
    );
  });

  readonly totalPaginas = computed(() =>
    Math.max(1, Math.ceil(this.subcategoriasFiltradas().length / TAMANHO_PAGINA)),
  );

  readonly subcategoriasPagina = computed(() => {
    const inicio = this.pagina() * TAMANHO_PAGINA;
    return this.subcategoriasFiltradas().slice(inicio, inicio + TAMANHO_PAGINA);
  });

  readonly intervaloExibido = computed(() => {
    const total = this.subcategoriasFiltradas().length;
    if (total === 0) return '0';
    const inicio = this.pagina() * TAMANHO_PAGINA + 1;
    return `${inicio}-${Math.min(inicio + TAMANHO_PAGINA - 1, total)}`;
  });

  ngOnInit(): void {
    this.categoriaService
      .listar(0, 1000)
      .subscribe((p) => this.categorias.set(p.items.filter((x) => x.nome)));
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.subcategoriaService.listar(null, 0, 1000).subscribe({
      next: (pagina) => {
        this.subcategorias.set(pagina.items);
        this.carregando.set(false);
      },
      error: () => {
        this.erro.set('Não foi possível carregar as subcategorias. Verifique se a API está no ar.');
        this.carregando.set(false);
      },
    });
  }

  aoDigitarBusca(valor: string): void {
    this.termoBusca.set(valor);
    this.pagina.set(0);
  }

  aoMudarCategoria(valor: string): void {
    this.filtroCategoria.set(valor ? Number(valor) : null);
    this.pagina.set(0);
  }

  aoMudarStatus(valor: string): void {
    this.filtroStatus.set(valor ? Number(valor) : null);
    this.pagina.set(0);
  }

  irParaPagina(p: number): void {
    if (p >= 0 && p < this.totalPaginas()) this.pagina.set(p);
  }

  excluir(evento: Event, subcategoria: Subcategoria): void {
    evento.preventDefault();
    evento.stopPropagation();
    if (!confirm(`Excluir a subcategoria "${subcategoria.nome}"? Essa ação não pode ser desfeita.`))
      return;

    this.subcategoriaService.excluir(subcategoria.id).subscribe({
      next: () => this.subcategorias.update((l) => l.filter((s) => s.id !== subcategoria.id)),
      error: (err) => alert(err.error?.detail ?? 'Não foi possível excluir esta subcategoria.'),
    });
  }

  statusLabel(status: StatusRef): string {
    return status.nome;
  }

  corStatus(status: StatusRef): string {
    if (status.id === STATUS_PUBLICACAO.ATIVA) return 'var(--ok)';
    return status.id === STATUS_PUBLICACAO.INATIVA ? 'var(--dim2)' : 'var(--warn)';
  }
}
