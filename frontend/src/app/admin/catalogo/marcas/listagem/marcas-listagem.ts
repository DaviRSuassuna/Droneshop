import { baixarArquivo } from '../../../../shared/arquivo';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MarcaService } from '../../../../core/services/marca.service';
import { Marca } from '../../../../core/models/marca.model';
import { STATUS_PUBLICACAO, StatusRef } from '../../../../core/models/status.model';

const TAMANHO_PAGINA = 10;

@Component({
  selector: 'app-marcas-listagem',
  imports: [RouterLink],
  templateUrl: './marcas-listagem.html',
  styleUrl: './marcas-listagem.scss',
})
export class MarcasListagem implements OnInit {
  private readonly marcaService = inject(MarcaService);
  readonly STATUS = STATUS_PUBLICACAO;

  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly marcas = signal<Marca[]>([]);

  readonly termoBusca = signal('');
  readonly filtroPais = signal('');
  readonly filtroStatus = signal<number | null>(null);
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
      const combinaStatus = !status || m.status.id === status;
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
    this.marcaService.listar(0, 1000).subscribe({
      next: (pagina) => {
        this.marcas.set(pagina.items);
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
    this.filtroStatus.set(valor ? Number(valor) : null);
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

  statusLabel(status: StatusRef): string {
    return status.nome;
  }

  exportarCsv(): void {
    this.marcaService
      .exportar({
        busca: this.termoBusca(),
        idStatus: this.filtroStatus(),
        paisOrigem: this.filtroPais(),
      })
      .subscribe({
        next: (blob) => baixarArquivo('marcas.csv', blob),
        error: () => this.erro.set('Não foi possível exportar o CSV.'),
      });
  }
}
