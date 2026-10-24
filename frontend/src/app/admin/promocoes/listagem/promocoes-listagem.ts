import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ESCOPO_PROMOCAO, Promocao } from '../../../core/models/promocao.model';
import { PromocaoService } from '../../../core/services/promocao.service';

type FiltroSituacao = 'TODAS' | 'VIGENTE' | 'AGENDADA' | 'ENCERRADA';

@Component({
  selector: 'app-promocoes-listagem',
  imports: [RouterLink],
  templateUrl: './promocoes-listagem.html',
  styleUrl: './promocoes-listagem.scss',
})
export class PromocoesListagem implements OnInit {
  private readonly promocaoService = inject(PromocaoService);

  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly promocoes = signal<Promocao[]>([]);
  readonly termoBusca = signal('');
  readonly filtroEscopo = signal<number | null>(null);
  readonly filtroVigencia = signal('');
  readonly aba = signal<FiltroSituacao>('TODAS');

  readonly escopos = [
    { id: ESCOPO_PROMOCAO.PRODUTO, nome: 'Produto' },
    { id: ESCOPO_PROMOCAO.CARRINHO, nome: 'Carrinho' },
    { id: ESCOPO_PROMOCAO.FRETE, nome: 'Frete' },
  ];

  readonly filtradas = computed(() => {
    const termo = this.termoBusca().trim().toLocaleLowerCase('pt-BR');
    const escopo = this.filtroEscopo();
    const vigencia = this.filtroVigencia();
    const aba = this.aba();
    return this.promocoes().filter((promocao) => {
      const combinaNome = !termo || promocao.nome.toLocaleLowerCase('pt-BR').includes(termo);
      const combinaEscopo = escopo === null || promocao.escopo.id === escopo;
      const combinaVigencia =
        !vigencia ||
        (vigencia === 'HOJE' && this.situacao(promocao) === 'VIGENTE') ||
        (vigencia === 'FUTURAS' && this.situacao(promocao) === 'AGENDADA');
      const combinaAba =
        aba === 'TODAS' ||
        (aba === 'VIGENTE' && this.situacao(promocao) === 'VIGENTE') ||
        this.situacao(promocao) === aba;
      return combinaNome && combinaEscopo && combinaVigencia && combinaAba;
    });
  });

  readonly quantidadeAtivas = computed(
    () => this.promocoes().filter((p) => this.situacao(p) === 'VIGENTE').length,
  );
  readonly quantidadeAgendadas = computed(
    () => this.promocoes().filter((p) => this.situacao(p) === 'AGENDADA').length,
  );
  readonly quantidadeEncerradas = computed(
    () => this.promocoes().filter((p) => this.situacao(p) === 'ENCERRADA').length,
  );
  readonly produtosEmOferta = computed(() =>
    this.promocoes()
      .filter((p) => this.situacao(p) === 'VIGENTE')
      .reduce((total, promocao) => total + (promocao.produtos?.length ?? 0), 0),
  );

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.promocaoService.listar('', 0, 1000).subscribe({
      next: (pagina) => {
        this.promocoes.set(pagina.items ?? []);
        this.carregando.set(false);
      },
      error: () => {
        this.erro.set('Não foi possível carregar as promoções. Verifique se a API está no ar.');
        this.carregando.set(false);
      },
    });
  }

  selecionarAba(evento: Event, aba: FiltroSituacao): void {
    evento.preventDefault();
    this.aba.set(aba);
  }

  aoMudarEscopo(valor: string): void {
    this.filtroEscopo.set(valor ? Number(valor) : null);
  }

  statusRotulo(promocao: Promocao): string {
    const status = this.situacao(promocao);
    return status === 'VIGENTE'
      ? 'Ativa'
      : status === 'AGENDADA'
        ? 'Agendada'
        : status === 'ENCERRADA'
          ? 'Encerrada'
          : 'Inativa';
  }

  statusCor(promocao: Promocao): string {
    const status = this.situacao(promocao);
    return status === 'VIGENTE'
      ? 'var(--ok)'
      : status === 'AGENDADA'
        ? 'var(--info)'
        : 'var(--mute)';
  }

  rotuloEscopo(id: number): string {
    return this.escopos.find((escopo) => escopo.id === id)?.nome ?? 'Outro';
  }

  descontoRotulo(promocao: Promocao): string {
    if (
      promocao.escopo.id === ESCOPO_PROMOCAO.FRETE &&
      !promocao.percentualDesconto &&
      !promocao.valorDesconto
    ) {
      return 'Frete zerado';
    }
    if (promocao.percentualDesconto != null) {
      const desconto = `−${promocao.percentualDesconto}%`;
      if (promocao.escopo.id === ESCOPO_PROMOCAO.CARRINHO) {
        return promocao.valorMinimoCarrinho != null
          ? `${desconto} acima de ${this.moeda(promocao.valorMinimoCarrinho)}`
          : `${desconto} no carrinho`;
      }
      if (promocao.escopo.id === ESCOPO_PROMOCAO.FRETE) return `${desconto} no frete`;
      return `${desconto} em ${promocao.produtos?.length ?? 0} ${promocao.produtos?.length === 1 ? 'item' : 'itens'}`;
    }
    if (promocao.valorDesconto != null) {
      return `${this.moeda(promocao.valorDesconto)} de desconto`;
    }
    return 'Desconto configurado';
  }

  vigencia(promocao: Promocao): string {
    return `${this.dataCurta(promocao.dataInicio)} a ${this.dataCurta(promocao.dataFim)}`;
  }

  private situacao(promocao: Promocao): string {
    return promocao.situacao ?? (promocao.ativa ? 'VIGENTE' : 'INATIVA');
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

  private moeda(valor: number): string {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor);
  }
}
