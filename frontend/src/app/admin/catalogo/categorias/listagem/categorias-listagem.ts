import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CategoriaService } from '../../../../core/services/categoria.service';
import { Categoria } from '../../../../core/models/categoria.model';
import { STATUS_PUBLICACAO, StatusRef } from '../../../../core/models/status.model';
import { TIPO_PRODUTO } from '../../../../core/models/produto.model';

const TAMANHO_PAGINA = 10;

@Component({
  selector: 'app-categorias-listagem',
  imports: [RouterLink],
  templateUrl: './categorias-listagem.html',
  styleUrl: './categorias-listagem.scss',
})
export class CategoriasListagem implements OnInit {
  private readonly categoriaService = inject(CategoriaService);

  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly categorias = signal<Categoria[]>([]);

  readonly termoBusca = signal('');
  readonly filtroTipo = signal<number | null>(null);
  readonly reordenando = signal(false);
  readonly salvandoOrdem = signal(false);
  private ordemOriginal: Categoria[] = [];

  readonly STATUS = STATUS_PUBLICACAO;
  readonly TIPO = TIPO_PRODUTO;
  readonly filtroStatus = signal<number | null>(null);
  readonly pagina = signal(0);

  readonly categoriasFiltradas = computed(() => {
    const termo = this.termoBusca().trim().toLowerCase();
    const status = this.filtroStatus();

    return this.categorias().filter((c) => {
      const combinaTermo =
        !termo || c.nome.toLowerCase().includes(termo) || c.slug.toLowerCase().includes(termo);
      const combinaStatus = !status || c.status.id === status;
      return (
        combinaTermo && combinaStatus && (!this.filtroTipo() || c.tipo?.id === this.filtroTipo())
      );
    });
  });

  readonly totalPaginas = computed(() =>
    Math.max(1, Math.ceil(this.categoriasFiltradas().length / TAMANHO_PAGINA)),
  );

  readonly categoriasPagina = computed(() => {
    const inicio = this.pagina() * TAMANHO_PAGINA;
    return this.categoriasFiltradas().slice(inicio, inicio + TAMANHO_PAGINA);
  });

  readonly intervaloExibido = computed(() => {
    const total = this.categoriasFiltradas().length;
    if (total === 0) return '0-0';
    const inicio = this.pagina() * TAMANHO_PAGINA + 1;
    const fim = Math.min(inicio + TAMANHO_PAGINA - 1, total);
    return `${inicio}-${fim}`;
  });

  ngOnInit(): void {
    this.carregarCategorias();
  }

  carregarCategorias(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.categoriaService.listar(0, 1000).subscribe({
      next: (pagina) => {
        this.categorias.set(
          [...pagina.items].sort((a, b) => a.ordemExibicao - b.ordemExibicao || a.id - b.id),
        );
        this.carregando.set(false);
      },
      error: () => {
        this.erro.set('Não foi possível carregar as categorias. Verifique se a API está no ar.');
        this.carregando.set(false);
      },
    });
  }

  aoDigitarBusca(valor: string): void {
    this.termoBusca.set(valor);
    this.pagina.set(0);
  }

  aoMudarStatus(valor: string): void {
    this.filtroStatus.set(valor ? Number(valor) : null);
    this.pagina.set(0);
  }

  paginaAnterior(): void {
    this.pagina.update((p) => Math.max(0, p - 1));
  }

  proximaPagina(): void {
    this.pagina.update((p) => Math.min(this.totalPaginas() - 1, p + 1));
  }

  excluir(evento: Event, categoria: Categoria): void {
    evento.preventDefault();
    evento.stopPropagation();
    if (!confirm(`Excluir a categoria "${categoria.nome}"? Essa ação não pode ser desfeita.`))
      return;

    this.categoriaService.excluir(categoria.id).subscribe({
      next: () => this.categorias.update((lista) => lista.filter((c) => c.id !== categoria.id)),
      error: (err) => alert(err.error?.detail ?? 'Não foi possível excluir esta categoria.'),
    });
  }

  statusLabel(status: StatusRef): string {
    return status.nome;
  }

  iniciarOrdem(): void {
    this.ordemOriginal = [...this.categorias()];
    this.termoBusca.set('');
    this.filtroStatus.set(null);
    this.filtroTipo.set(null);
    this.pagina.set(0);
    this.reordenando.set(true);
  }

  mover(categoria: Categoria, delta: number): void {
    const lista = [...this.categorias()];
    const i = lista.findIndex((c) => c.id === categoria.id);
    const destino = i + delta;
    if (destino < 0 || destino >= lista.length) return;
    [lista[i], lista[destino]] = [lista[destino], lista[i]];
    this.categorias.set(lista);
  }

  cancelarOrdem(): void {
    this.categorias.set(this.ordemOriginal);
    this.reordenando.set(false);
  }

  salvarOrdem(): void {
    this.salvandoOrdem.set(true);
    this.categoriaService.reordenar(this.categorias().map((c) => c.id)).subscribe({
      next: () => {
        this.salvandoOrdem.set(false);
        this.reordenando.set(false);
        this.carregarCategorias();
      },
      error: () => {
        this.salvandoOrdem.set(false);
        this.erro.set('Não foi possível salvar a ordem.');
      },
    });
  }

  exportarCsv(): void {
    const linhas = [
      ['Nome', 'Slug', 'Ordem', 'Status', 'Exibir no menu'],
      ...this.categoriasFiltradas().map((c) => [
        c.nome,
        c.slug,
        String(c.ordemExibicao),
        this.statusLabel(c.status),
        c.exibirMenu ? 'Sim' : 'Nao',
      ]),
    ];
    const csv = linhas
      .map((linha) => linha.map((v) => `"${v.replace(/"/g, '""')}"`).join(','))
      .join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'categorias.csv';
    a.click();
    URL.revokeObjectURL(url);
  }
}
