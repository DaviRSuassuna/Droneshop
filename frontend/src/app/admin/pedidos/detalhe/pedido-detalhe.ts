import { baixarArquivo } from '../../../shared/arquivo';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PedidoService } from '../../../core/services/pedido.service';
import {
  EXIBICAO_STATUS_PEDIDO,
  HistoricoStatusPedido,
  ORIGEM_REGISTRO,
  Pedido,
  STATUS_PEDIDO,
  podeMudarStatus,
} from '../../../core/models/pedido.model';
import { CepPipe } from '../../../shared/pipes/cep.pipe';

/** Caminho normal do pedido, para mostrar as próximas etapas na linha do tempo. */
const FLUXO_NORMAL: readonly number[] = [
  STATUS_PEDIDO.PENDENTE,
  STATUS_PEDIDO.PAGO,
  STATUS_PEDIDO.SEPARACAO,
  STATUS_PEDIDO.ENVIADO,
  STATUS_PEDIDO.ENTREGUE,
];

/** Detalhe do pedido no admin. Rastreio chega com a entrega (#31); etiqueta local usa o endereço congelado. */
@Component({
  selector: 'app-pedido-detalhe',
  imports: [RouterLink, DatePipe, CepPipe],
  templateUrl: './pedido-detalhe.html',
  styleUrl: './pedido-detalhe.scss',
})
export class PedidoDetalhe implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly pedidoService = inject(PedidoService);

  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly pedido = signal<Pedido | null>(null);
  readonly historico = signal<HistoricoStatusPedido[]>([]);
  readonly erroHistorico = signal(false);
  readonly salvando = signal(false);
  readonly erroAcao = signal<string | null>(null);

  readonly podeCancelar = computed(() => this.permite(STATUS_PEDIDO.CANCELADO));
  readonly podeEnviar = computed(() => this.permite(STATUS_PEDIDO.ENVIADO));

  /** Etapas do caminho normal que ainda não aconteceram. */
  readonly proximasEtapas = computed(() => {
    const atual = this.pedido()?.status.id;
    const i = atual === undefined ? -1 : FLUXO_NORMAL.indexOf(atual);
    return i < 0 ? [] : FLUXO_NORMAL.slice(i + 1);
  });

  readonly subtotal = computed(() =>
    (this.pedido()?.itens ?? []).reduce((soma, i) => soma + Number(i.subtotal ?? 0), 0),
  );

  private readonly formatoMoeda = new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  });

  readonly gerandoEtiqueta = signal(false);

  imprimirEtiqueta(): void {
    const pedido = this.pedido();
    if (!pedido || this.gerandoEtiqueta()) return;
    this.gerandoEtiqueta.set(true);
    this.erroAcao.set(null);
    this.pedidoService.etiqueta(pedido.id).subscribe({
      next: (blob) => {
        this.gerandoEtiqueta.set(false);
        baixarArquivo('etiqueta-' + pedido.id + '.pdf', blob);
      },
      error: () => {
        this.gerandoEtiqueta.set(false);
        this.erroAcao.set(
          'Não foi possível gerar a etiqueta. Confira se o pedido tem endereço de entrega.',
        );
      },
    });
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isInteger(id) || id <= 0) {
      this.erro.set('Pedido não encontrado.');
      this.carregando.set(false);
      return;
    }
    this.carregar(id);
  }

  cancelar(): void {
    const p = this.pedido();
    if (!p || !this.podeCancelar()) return;
    if (!confirm(`Cancelar o pedido #${p.numero}? Essa ação não pode ser desfeita.`)) return;
    this.mudarStatus(p.id, STATUS_PEDIDO.CANCELADO);
  }

  marcarComoEnviado(): void {
    const p = this.pedido();
    if (!p || !this.podeEnviar()) return;
    this.mudarStatus(p.id, STATUS_PEDIDO.ENVIADO);
  }

  tituloEvento(h: HistoricoStatusPedido): string {
    return h.statusAnterior ? this.rotuloStatus(h.statusNovo.id) : 'Pedido criado';
  }

  /** Quem fez a mudança: o autor quando manual; sistema ou integração nos demais (D11). */
  responsavel(h: HistoricoStatusPedido): string {
    if (h.origem.id === ORIGEM_REGISTRO.MANUAL) return h.autor ? `por ${h.autor.nome}` : 'manual';
    return h.origem.id === ORIGEM_REGISTRO.INTEGRACAO ? 'integração' : 'sistema';
  }

  rotuloStatus(id: number): string {
    return EXIBICAO_STATUS_PEDIDO[id]?.rotulo ?? '';
  }

  corStatus(id: number): string {
    return EXIBICAO_STATUS_PEDIDO[id]?.cor ?? 'var(--mute)';
  }

  moeda(valor: number | null | undefined): string {
    return this.formatoMoeda.format(Number(valor ?? 0));
  }

  private permite(novo: number): boolean {
    const p = this.pedido();
    return !!p && !this.salvando() && podeMudarStatus(p.status.id, novo);
  }

  private carregar(id: number): void {
    this.pedidoService.buscarPorId(id).subscribe({
      next: (p) => {
        this.pedido.set(p);
        this.carregando.set(false);
      },
      error: (e: HttpErrorResponse) => {
        this.erro.set(e.status === 404 ? 'Pedido não encontrado.' : 'Erro ao carregar o pedido.');
        this.carregando.set(false);
      },
    });
    this.pedidoService.historico(id).subscribe({
      next: (h) => {
        this.historico.set(h);
        this.erroHistorico.set(false);
      },
      error: () => this.erroHistorico.set(true),
    });
  }

  private mudarStatus(id: number, idStatus: number): void {
    this.salvando.set(true);
    this.erroAcao.set(null);
    this.pedidoService.alterarStatus(id, idStatus).subscribe({
      next: () => {
        this.salvando.set(false);
        this.carregar(id);
      },
      error: (e: HttpErrorResponse) => {
        this.salvando.set(false);
        if (e.status === 409) {
          this.erroAcao.set(
            e.error?.detail ?? 'Essa mudança de status não é permitida para o pedido agora.',
          );
          // o status pode ter mudado em outra sessão
          this.carregar(id);
        } else {
          this.erroAcao.set(
            e.error?.detail ?? 'Não foi possível alterar o status. Tente novamente.',
          );
        }
      },
    });
  }
}
