import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AdminNav } from '../../../../layout/admin/admin-nav/admin-nav';
import { CategoriaService } from '../../../../core/services/categoria.service';
import { Categoria, StatusCategoria } from '../../../../core/models/categoria.model';

const TAMANHO_PAGINA = 10;

@Component({
  selector: 'app-categorias-listagem',
  standalone: true,
  imports: [RouterLink, AdminNav],
  templateUrl: './categorias-listagem.html',
  styleUrl: './categorias-listagem.scss',
})
export class CategoriasListagem implements OnInit {
  private readonly categoriaService = inject(CategoriaService);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly categorias = signal<Categoria[]>([]);
  readonly termoBusca = signal('');
  readonly filtroStatus = signal<StatusCategoria | ''>('');
  readonly pagina = signal(0);

  readonly categoriasFiltradas = computed(() => {
    const termo = this.termoBusca().trim().toLowerCase();
    const status = this.filtroStatus();
    return this.categorias().filter((categoria) =>
      (!termo || categoria.nome.toLowerCase().includes(termo) || categoria.slug.toLowerCase().includes(termo)) &&
      (!status || categoria.status === status),
    );
  });
  readonly totalPaginas = computed(() => Math.max(1, Math.ceil(this.categoriasFiltradas().length / TAMANHO_PAGINA)));
  readonly categoriasPagina = computed(() => {
    const inicio = this.pagina() * TAMANHO_PAGINA;
    return this.categoriasFiltradas().slice(inicio, inicio + TAMANHO_PAGINA);
  });
  readonly intervaloExibido = computed(() => {
    const total = this.categoriasFiltradas().length;
    if (!total) return '0-0';
    const inicio = this.pagina() * TAMANHO_PAGINA + 1;
    return `${inicio}-${Math.min(inicio + TAMANHO_PAGINA - 1, total)}`;
  });

  ngOnInit(): void { this.carregarCategorias(); }
  carregarCategorias(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.categoriaService.listar().subscribe({
      next: (categorias) => { this.categorias.set(categorias); this.carregando.set(false); },
      error: () => { this.erro.set('Não foi possível carregar as categorias. Verifique se a API está no ar.'); this.carregando.set(false); },
    });
  }
  aoDigitarBusca(valor: string): void { this.termoBusca.set(valor); this.pagina.set(0); }
  aoMudarStatus(valor: string): void { this.filtroStatus.set(valor as StatusCategoria | ''); this.pagina.set(0); }
  paginaAnterior(): void { this.pagina.update((pagina) => Math.max(0, pagina - 1)); }
  proximaPagina(): void { this.pagina.update((pagina) => Math.min(this.totalPaginas() - 1, pagina + 1)); }
  statusLabel(status: StatusCategoria): string { return { RASCUNHO: 'Rascunho', ATIVA: 'Ativa', INATIVA: 'Inativa' }[status]; }
  excluir(evento: Event, categoria: Categoria): void {
    evento.preventDefault(); evento.stopPropagation();
    if (!confirm(`Excluir a categoria "${categoria.nome}"? Essa ação não pode ser desfeita.`)) return;
    this.categoriaService.excluir(categoria.id).subscribe({
      next: () => this.categorias.update((lista) => lista.filter((item) => item.id !== categoria.id)),
      error: () => alert('Não foi possível excluir esta categoria. Ela pode estar vinculada a produtos ou subcategorias.'),
    });
  }
}
