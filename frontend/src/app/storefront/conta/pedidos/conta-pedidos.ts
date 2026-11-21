import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { RESOLUCAO_DEVOLUCAO } from '../../../core/models/compra.model';
import {
  EXIBICAO_STATUS_PEDIDO,
  NotaFiscal,
  Pedido,
  PedidoListagem,
  Rastreio,
  STATUS_PEDIDO,
} from '../../../core/models/pedido.model';
import { ProblemDetail } from '../../../core/models/problem.model';
import { ContaPedidoService } from '../../../core/services/conta-pedido.service';
import { reais } from '../../../core/utils/moeda';
import { AccountMenu } from '../../../layout/storefront/account-menu/account-menu';
import { ContaSessao } from '../conta-sessao';

/** Classe do selo de status, como no protótipo (st ok / info / warn). */
function tom(id: number): string {
  if (id === STATUS_PEDIDO.ENTREGUE || id === STATUS_PEDIDO.PAGO) return 'ok';
  if (
    id === STATUS_PEDIDO.CANCELADO ||
    id === STATUS_PEDIDO.EM_DEVOLUCAO ||
    id === STATUS_PEDIDO.PENDENTE
  )
    return 'warn';
  return 'info';
}

/**
 * "Minha conta > Meus pedidos" (#29, protótipo 02 conta-pedidos.html): lista, rastreio (#31),
 * notas (#35), pagar o pendente (#27) e pedir devolução do entregue (#37).
 */
@Component({
  selector: 'app-conta-pedidos',
  imports: [AccountMenu, RouterLink, DatePipe],
  providers: [ContaSessao],
  templateUrl: './conta-pedidos.html',
  styleUrl: './conta-pedidos.scss',
})
export class ContaPedidos implements OnInit {
  protected readonly sessao = inject(ContaSessao);
  private readonly pedidos = inject(ContaPedidoService);

  protected readonly reais = reais;
  protected readonly tom = tom;
  protected readonly exibicao = EXIBICAO_STATUS_PEDIDO;

  readonly lista = signal<PedidoListagem[]>([]);
  readonly total = signal(0);
  readonly atual = signal<Pedido | null>(null);
  readonly rastreio = signal<Rastreio | null>(null);
  readonly notas = signal<NotaFiscal[]>([]);
  readonly mensagem = signal<{ texto: string; ok: boolean } | null>(null);

  readonly emAndamento = computed(
    () =>
      this.lista().filter(
        (p) =>
          p.status.id !== STATUS_PEDIDO.ENTREGUE &&
          p.status.id !== STATUS_PEDIDO.CANCELADO &&
          p.status.id !== STATUS_PEDIDO.DEVOLVIDO,
      ).length,
  );
  readonly pendente = computed(() => this.atual()?.status.id === STATUS_PEDIDO.PENDENTE);
  readonly entregue = computed(() => this.atual()?.status.id === STATUS_PEDIDO.ENTREGUE);

  ngOnInit(): void {
    this.sessao.carregar();
    this.pedidos.listar(0, 50).subscribe((pg) => {
      this.lista.set(pg.items);
      this.total.set(pg.totalItems);
      if (pg.items[0]) this.abrir(pg.items[0].id);
    });
  }

  abrir(id: number): void {
    this.mensagem.set(null);
    this.pedidos.buscar(id).subscribe((p) => this.atual.set(p));
    this.pedidos.rastreio(id).subscribe((r) => this.rastreio.set(r));
    this.pedidos.notas(id).subscribe((n) => this.notas.set(n));
  }

  pagarPix(): void {
    const p = this.atual();
    if (!p) return;
    this.pedidos.pagar(p.id, { idMetodo: p.metodoPagamento?.id ?? 1 }).subscribe({
      next: () => {
        this.mensagem.set({
          texto: 'Pagamento gerado. Assim que for aprovado, o pedido segue.',
          ok: true,
        });
        this.abrir(p.id);
      },
      error: (e: HttpErrorResponse) => this.mensagem.set({ texto: this.texto(e), ok: false }),
    });
  }

  devolver(): void {
    const p = this.atual();
    if (!p) return;
    this.pedidos
      .solicitarDevolucao({
        idPedido: p.id,
        idResolucao: RESOLUCAO_DEVOLUCAO.REEMBOLSO,
        motivo: 'Arrependimento',
        itens: p.itens.map((i) => ({ idItemPedido: i.id, quantidade: i.quantidade })),
      })
      .subscribe({
        next: () =>
          this.mensagem.set({ texto: 'Devolução solicitada. Acompanhe pelo e-mail.', ok: true }),
        error: (e: HttpErrorResponse) => this.mensagem.set({ texto: this.texto(e), ok: false }),
      });
  }

  baixar(caminho: string): void {
    this.pedidos.arquivo(caminho).subscribe((blob) => {
      const url = URL.createObjectURL(blob);
      window.open(url, '_blank', 'noopener');
      setTimeout(() => URL.revokeObjectURL(url), 60_000);
    });
  }

  copiar(codigo: string): void {
    void navigator.clipboard?.writeText(codigo);
  }

  private texto(e: HttpErrorResponse): string {
    const p = e.error as ProblemDetail | null;
    return p?.errors?.[0]?.message ?? p?.detail ?? 'Não foi possível concluir. Tente novamente.';
  }
}
