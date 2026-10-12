import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AdminNav } from '../../../../layout/admin/admin-nav/admin-nav';
import { MarcaService } from '../../../../core/services/marca.service';
import { Marca, StatusMarca } from '../../../../core/models/marca.model';

const TAMANHO_PAGINA = 10;

@Component({
  selector: 'app-marcas-listagem',
  standalone: true,
  imports: [RouterLink, AdminNav],
  templateUrl: './marcas-listagem.html',
  styleUrl: './marcas-listagem.scss',
})
export class MarcasListagem implements OnInit {
  private readonly marcaService = inject(MarcaService);

  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly marcas = signal<Marca[]>([]);

  readonly termoBusca = signal('');
  readonly filtroPais = signal('');
  readonly filtroStatus = signal<StatusMarca | ''>('');
  readonly pagina = signal(0);

  readonly paisesDisponiveis = computed(() => {
    const paises = new Set(
      this.marcas()
        .map((m) => m.paisOrigem)
        .filter(Boolean),
    );
    return Array.from(paises).sort();
  });

  readonly marcasFiltradas = computed(() => {
    const termo = this.termoBusca().trim().toLowerCase();
    const pais = this.filtroPais();
    const status = this.filtroStatus();

    return this.marcas().filter((m) => {
      const combinaTermo =
        !termo || m.nome.toLowerCase().includes(termo) || m.slug.toLowerCase().includes(termo);
      const combinaPais = !pais || m.paisOrigem === pais;
      const combinaStatus = !status || m.status === status;
      return combinaTermo && combinaPais && combinaStatus;
    });
  });

  readonly totalPaginas = computed(() =>
    Math.max(1, Math.ceil(this.marcasFiltradas().length / TAMANHO_PAGINA)),
  );

  readonly marcasPagina = computed(() => {
    const inicio = this.pagina() * TAMANHO_PAGINA;
    return this.marcasFiltradas().slice(inicio, inicio + TAMANHO_PAGINA);
  });

  readonly intervaloExibido = computed(() => {
    const total = this.marcasFiltradas().length;
    if (total === 0) return '0-0';
    const inicio = this.pagina() * TAMANHO_PAGINA + 1;
    const fim = Math.min(inicio + TAMANHO_PAGINA - 1, total);
    return `${inicio}-${fim}`;
  });

  ngOnInit(): void {
    this.carregarMarcas();
  }

  carregarMarcas(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.marcaService.listar(0, 500).subscribe({
      next: (marcas) => {
        this.marcas.set(marcas);
        this.carregando.set(false);
      },
      error: () => {
        this.erro.set('Não foi possível carregar as marcas. Verifique se a API está no ar.');
        this.carregando.set(false);
      },
    });
  }

  aoDigitarBusca(valor: string): void {
    this.termoBusca.set(valor);
    this.pagina.set(0);
  }

  aoMudarPais(valor: string): void {
    this.filtroPais.set(valor);
    this.pagina.set(0);
  }

  aoMudarStatus(valor: string): void {
    this.filtroStatus.set(valor as StatusMarca | '');
    this.pagina.set(0);
  }

  paginaAnterior(): void {
    this.pagina.update((p) => Math.max(0, p - 1));
  }

  proximaPagina(): void {
    this.pagina.update((p) => Math.min(this.totalPaginas() - 1, p + 1));
  }

  excluir(evento: Event, marca: Marca): void {
    evento.preventDefault();
    evento.stopPropagation();
    if (!confirm(`Excluir a marca "${marca.nome}"? Essa ação não pode ser desfeita.`)) return;

    this.marcaService.excluir(marca.id).subscribe({
      next: () => this.marcas.update((lista) => lista.filter((m) => m.id !== marca.id)),
      error: (err) => alert(err.error?.detail ?? 'Não foi possível excluir esta marca.'),
    });
  }

  statusLabel(status: StatusMarca): string {
    return { RASCUNHO: 'Rascunho', ATIVA: 'Ativa', INATIVA: 'Inativa' }[status];
  }

  exportarCsv(): void {
    const linhas = [
      ['Nome', 'Slug', 'Pais de origem', 'Status', 'Destaque na home'],
      ...this.marcasFiltradas().map((m) => [
        m.nome,
        m.slug,
        m.paisOrigem,
        this.statusLabel(m.status),
        m.destacarHome ? 'Sim' : 'Nao',
      ]),
    ];
    const csv = linhas
      .map((linha) => linha.map((v) => `"${v.replace(/"/g, '""')}"`).join(','))
      .join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'marcas.csv';
    a.click();
    URL.revokeObjectURL(url);
  }
}
