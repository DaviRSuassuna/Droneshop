import { baixarArquivo } from '../../../../shared/arquivo';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FornecedorService } from '../../../../core/services/fornecedor.service';
import { Fornecedor } from '../../../../core/models/fornecedor.model';
import { STATUS_FORNECEDOR, StatusRef } from '../../../../core/models/status.model';
import { CnpjPipe } from '../../../../shared/pipes/cnpj.pipe';

const TAMANHO_PAGINA = 10;

@Component({
  selector: 'app-fornecedores-listagem',
  imports: [RouterLink, CnpjPipe],
  templateUrl: './fornecedores-listagem.html',
  styleUrl: './fornecedores-listagem.scss',
})
export class FornecedoresListagem implements OnInit {
  private readonly fornecedorService = inject(FornecedorService);

  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly fornecedores = signal<Fornecedor[]>([]);

  readonly termoBusca = signal('');
  readonly filtroUf = signal('');
  readonly ufs = computed(() =>
    [
      ...new Set(
        this.fornecedores()
          .map((f) => f.endereco?.municipio.estado.sigla)
          .filter((uf): uf is string => !!uf),
      ),
    ].sort(),
  );
  readonly STATUS = STATUS_FORNECEDOR;
  readonly filtroStatus = signal<number | null>(null);
  readonly pagina = signal(0);

  readonly fornecedoresFiltrados = computed(() => {
    const termo = this.termoBusca().trim().toLowerCase();
    const termoDigitos = termo.replace(/\D/g, '');
    const status = this.filtroStatus();

    return this.fornecedores().filter((f) => {
      // CNPJ compara so os digitos, para a busca funcionar com ou sem mascara
      const combinaTermo =
        !termo ||
        f.razaoSocial.toLowerCase().includes(termo) ||
        f.emailContato.toLowerCase().includes(termo) ||
        (termoDigitos.length > 0 && f.cnpj.replace(/\D/g, '').includes(termoDigitos));
      const combinaStatus = !status || f.status.id === status;
      return (
        combinaTermo &&
        combinaStatus &&
        (!this.filtroUf() || f.endereco?.municipio.estado.sigla === this.filtroUf())
      );
    });
  });

  readonly totalPaginas = computed(() =>
    Math.max(1, Math.ceil(this.fornecedoresFiltrados().length / TAMANHO_PAGINA)),
  );

  readonly fornecedoresPagina = computed(() => {
    const inicio = this.pagina() * TAMANHO_PAGINA;
    return this.fornecedoresFiltrados().slice(inicio, inicio + TAMANHO_PAGINA);
  });

  readonly intervaloExibido = computed(() => {
    const total = this.fornecedoresFiltrados().length;
    if (total === 0) return '0-0';
    const inicio = this.pagina() * TAMANHO_PAGINA + 1;
    const fim = Math.min(inicio + TAMANHO_PAGINA - 1, total);
    return `${inicio}-${fim}`;
  });

  ngOnInit(): void {
    this.carregarFornecedores();
  }

  carregarFornecedores(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.fornecedorService.listar(0, 1000).subscribe({
      next: (pagina) => {
        this.fornecedores.set(pagina.items);
        this.carregando.set(false);
      },
      error: () => {
        this.erro.set('Não foi possível carregar os fornecedores. Verifique se a API está no ar.');
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

  excluir(evento: Event, fornecedor: Fornecedor): void {
    evento.preventDefault();
    evento.stopPropagation();
    if (
      !confirm(`Excluir o fornecedor "${fornecedor.razaoSocial}"? Essa ação não pode ser desfeita.`)
    )
      return;

    this.fornecedorService.excluir(fornecedor.id).subscribe({
      next: () => this.fornecedores.update((lista) => lista.filter((f) => f.id !== fornecedor.id)),
      error: (err) => alert(err.error?.detail ?? 'Não foi possível excluir este fornecedor.'),
    });
  }

  statusLabel(status: StatusRef): string {
    return status.nome;
  }

  exportarCsv(): void {
    this.fornecedorService
      .exportar({ busca: this.termoBusca(), idStatus: this.filtroStatus(), uf: this.filtroUf() })
      .subscribe({
        next: (blob) => baixarArquivo('fornecedores.csv', blob),
        error: () => this.erro.set('Não foi possível exportar o CSV.'),
      });
  }
}
