import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Pedido, STATUS_PEDIDO } from '../../../core/models/pedido.model';
import { ContaPedidoService } from '../../../core/services/conta-pedido.service';
import { reais } from '../../../core/utils/moeda';

/** Pedido confirmado (#26, protótipo 02 confirmacao.html). O id vem da rota. */
@Component({
  selector: 'app-confirmacao',
  imports: [RouterLink, DatePipe],
  templateUrl: './confirmacao.html',
  styleUrl: './confirmacao.scss',
})
export class Confirmacao implements OnInit {
  private readonly pedidos = inject(ContaPedidoService);

  private readonly rota = inject(ActivatedRoute);
  readonly pedido = signal<Pedido | null>(null);
  protected readonly reais = reais;

  readonly pago = computed(() => this.pedido()?.status.id === STATUS_PEDIDO.PAGO);
  readonly pagamento = computed(() => this.pedido()?.pagamentos.find((p) => p.vigente) ?? null);
  readonly unidades = computed(
    () => this.pedido()?.itens.reduce((s, i) => s + i.quantidade, 0) ?? 0,
  );

  ngOnInit(): void {
    this.pedidos
      .buscar(Number(this.rota.snapshot.paramMap.get('id')))
      .subscribe((p) => this.pedido.set(p));
  }

  primeiroNome(): string {
    return this.pedido()?.cliente.nome.split(' ')[0] ?? '';
  }

  metodo(): string {
    const m = this.pedido()?.metodoPagamento?.nome;
    return m === 'PIX'
      ? 'Pix'
      : m === 'BOLETO'
        ? 'Boleto'
        : m === 'CARTAO'
          ? 'Cartão de crédito'
          : (m ?? '');
  }
}
