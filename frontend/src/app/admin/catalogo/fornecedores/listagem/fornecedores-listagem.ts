import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AdminNav } from '../../../../layout/admin/admin-nav/admin-nav';
import { FornecedorService } from '../../../../core/services/fornecedor.service';
import { Fornecedor, StatusFornecedor } from '../../../../core/models/fornecedor.model';

const TAMANHO_PAGINA = 10;

@Component({
  selector: 'app-fornecedores-listagem',
  standalone: true,
  imports: [RouterLink, AdminNav],
  templateUrl: './fornecedores-listagem.html',
  styleUrl: './fornecedores-listagem.scss',
})
export class FornecedoresListagem implements OnInit {
  private readonly fornecedorService = inject(FornecedorService);

  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly fornecedores = signal<Fornecedor[]>([]);

  readonly termoBusca = signal('');
  readonly filtroStatus = signal<StatusFornecedor | ''>('');
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
        (termoDigitos.length > 0 && f.cnpj.replace(/\D/g, '').includes(termoDigitos));
      const combinaStatus = !status || f.status === status;
      return combinaTermo && combinaStatus;
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
    this.fornecedorService.listar(0, 500).subscribe({
      next: (fornecedores) => {
        this.fornecedores.set(fornecedores);
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
    this.filtroStatus.set(valor as StatusFornecedor | '');
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
      !confirm(
        `Excluir o fornecedor "${fornecedor.razaoSocial}"? Essa ação não pode ser desfeita.`,
      )
    )
      return;

    this.fornecedorService.excluir(fornecedor.id).subscribe({
      next: () =>
        this.fornecedores.update((lista) => lista.filter((f) => f.id !== fornecedor.id)),
      error: () =>
        alert('Não foi possível excluir este fornecedor. Ele pode estar vinculado a produtos.'),
    });
  }

  statusLabel(status: StatusFornecedor): string {
    return { ATIVO: 'Ativo', INATIVO: 'Inativo' }[status];
  }

  exportarCsv(): void {
    const linhas = [
      ['Razao social', 'CNPJ', 'Nome do contato', 'E-mail do contato', 'Telefone do contato', 'Status'],
      ...this.fornecedoresFiltrados().map((f) => [
        f.razaoSocial,
        f.cnpj,
        f.nomeContato,
        f.emailContato,
        f.telefoneContato ?? '',
        this.statusLabel(f.status),
      ]),
    ];
    const csv = linhas
      .map((linha) => linha.map((v) => `"${v.replace(/"/g, '""')}"`).join(','))
      .join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'fornecedores.csv';
    a.click();
    URL.revokeObjectURL(url);
  }
}
