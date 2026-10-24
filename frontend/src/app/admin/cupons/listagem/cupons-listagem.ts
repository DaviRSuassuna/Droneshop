import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { EMPTY, expand, finalize, reduce } from 'rxjs';
import { Cupom, UsoCupom, TIPO_DESCONTO_CUPOM } from '../../../core/models/cupom.model';
import { CupomService } from '../../../core/services/cupom.service';

@Component({
  selector: 'app-cupons-listagem',
  imports: [RouterLink],
  templateUrl: './cupons-listagem.html',
  styleUrl: './cupons-listagem.scss',
})
export class CuponsListagem implements OnInit {
  private readonly cupomService = inject(CupomService);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly cupons = signal<Cupom[]>([]);
  readonly exportando = signal<number | null>(null);
  readonly erroExportacao = signal<string | null>(null);
  readonly busca = signal('');
  readonly tipoFiltro = signal('');
  readonly situacaoFiltro = signal('');
  readonly filtrados = computed(() => {
    const busca = this.busca().trim().toUpperCase();
    return this.cupons().filter(
      (cupom) =>
        (!busca || cupom.codigo.includes(busca)) &&
        (!this.tipoFiltro() || cupom.tipoDesconto.id === Number(this.tipoFiltro())) &&
        (!this.situacaoFiltro() || cupom.situacao === this.situacaoFiltro()),
    );
  });
  readonly ativos = computed(() => this.cupons().filter((c) => c.situacao === 'VIGENTE').length);
  readonly agendados = computed(
    () => this.cupons().filter((c) => c.situacao === 'AGENDADO').length,
  );
  readonly usosTotais = computed(() =>
    this.cupons().reduce((total, cupom) => total + cupom.usos, 0),
  );

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.cupomService.listar('', '', 0, 1000).subscribe({
      next: (pagina) => {
        this.cupons.set(pagina.items ?? []);
        this.carregando.set(false);
      },
      error: () => {
        this.erro.set('Não foi possível carregar os cupons. Verifique se a API está no ar.');
        this.carregando.set(false);
      },
    });
  }

  tipoNome(cupom: Cupom): string {
    return cupom.tipoDesconto.id === TIPO_DESCONTO_CUPOM.PERCENTUAL ? 'Percentual' : 'Valor fixo';
  }

  valor(cupom: Cupom): string {
    return cupom.tipoDesconto.id === TIPO_DESCONTO_CUPOM.PERCENTUAL
      ? `${cupom.valor}%`
      : new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(cupom.valor);
  }

  vigencia(cupom: Cupom): string {
    const inicio = this.dataCurta(cupom.dataInicio);
    const fim = cupom.dataFim ? this.dataCurta(cupom.dataFim) : 'sem data final';
    return `${inicio} a ${fim}`;
  }

  usos(cupom: Cupom): string {
    return `${cupom.usos} / ${cupom.limiteTotalUso ?? 'ilimitado'}`;
  }

  percentualUso(cupom: Cupom): number {
    if (!cupom.limiteTotalUso || cupom.limiteTotalUso <= 0) return 0;
    return Math.min(100, Math.max(0, (cupom.usos / cupom.limiteTotalUso) * 100));
  }

  status(cupom: Cupom): string {
    return cupom.situacao === 'VIGENTE'
      ? 'Ativo'
      : cupom.situacao === 'AGENDADO'
        ? 'Agendado'
        : cupom.situacao === 'ESGOTADO'
          ? 'Esgotado'
          : cupom.situacao === 'INATIVO'
            ? 'Pausado'
            : 'Encerrado';
  }

  cor(cupom: Cupom): string {
    return cupom.situacao === 'VIGENTE'
      ? 'var(--ok)'
      : cupom.situacao === 'AGENDADO'
        ? 'var(--info)'
        : cupom.situacao === 'ESGOTADO'
          ? 'var(--warn)'
          : 'var(--mute)';
  }

  exportarUsos(cupom: Cupom): void {
    if (this.exportando() !== null) return;
    this.exportando.set(cupom.id);
    this.erroExportacao.set(null);
    this.cupomService
      .usos(cupom.id)
      .pipe(
        expand((pagina) =>
          pagina.page + 1 < pagina.totalPages
            ? this.cupomService.usos(cupom.id, pagina.page + 1)
            : EMPTY,
        ),
        reduce((usos, pagina) => [...usos, ...(pagina.items ?? [])], [] as UsoCupom[]),
        finalize(() => this.exportando.set(null)),
      )
      .subscribe({
        next: (usos) => {
          const linhas = [
            ['Cupom', 'Pedido', 'Cliente', 'Data de uso'],
            ...usos.map((uso) => [
              cupom.codigo,
              String(uso.pedido.id),
              uso.cliente.nome,
              uso.dataUso,
            ]),
          ];
          const csv = linhas
            .map((linha) => linha.map((v) => `"${v.replace(/"/g, '""')}"`).join(','))
            .join('\n');
          const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8;' }));
          const a = document.createElement('a');
          a.href = url;
          a.download = `usos-${cupom.codigo.toLowerCase()}.csv`;
          a.click();
          URL.revokeObjectURL(url);
        },
        error: (err) =>
          this.erroExportacao.set(
            err.error?.detail ?? 'Não foi possível exportar os usos do cupom. Tente novamente.',
          ),
      });
  }

  private dataCurta(valor: string): string {
    const data = new Date(valor);
    return Number.isNaN(data.getTime())
      ? '—'
      : new Intl.DateTimeFormat('pt-BR', {
          day: '2-digit',
          month: '2-digit',
          year: 'numeric',
        }).format(data);
  }
}
