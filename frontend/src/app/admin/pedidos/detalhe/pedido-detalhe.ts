import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AdminNav } from '../../../layout/admin/admin-nav/admin-nav';
import { PedidoService } from '../../../core/services/pedido.service';
import { EXIBICAO_STATUS_PEDIDO, Pedido } from '../../../core/models/pedido.model';
import { CepPipe } from '../../../shared/pipes/cep.pipe';

/** Detalhe do pedido no admin. Rastreio e ações chegam com a entrega (#31) e as transições (#17). */
@Component({
  selector: 'app-pedido-detalhe',
  standalone: true,
  imports: [RouterLink, AdminNav, DatePipe, CepPipe],
  templateUrl: './pedido-detalhe.html',
  styleUrl: './pedido-detalhe.scss',
})
export class PedidoDetalhe implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly pedidoService = inject(PedidoService);

  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly pedido = signal<Pedido | null>(null);

  readonly subtotal = computed(() =>
    (this.pedido()?.itens ?? []).reduce((soma, i) => soma + Number(i.subtotal ?? 0), 0),
  );

  private readonly formatoMoeda = new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isInteger(id) || id <= 0) {
      this.erro.set('Pedido não encontrado.');
      this.carregando.set(false);
      return;
    }
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
  }

  rotuloStatus(id: number): string {
    return EXIBICAO_STATUS_PEDIDO[id]?.rotulo ?? '';
  }

  corStatus(id: number): string {
    return EXIBICAO_STATUS_PEDIDO[id]?.cor ?? 'var(--dim2)';
  }

  moeda(valor: number | null | undefined): string {
    return this.formatoMoeda.format(Number(valor ?? 0));
  }
}
