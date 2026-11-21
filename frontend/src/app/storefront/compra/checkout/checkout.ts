import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { EnderecoCliente } from '../../../core/models/cliente.model';
import {
  CartaoSalvo,
  Carrinho,
  CotacaoFrete,
  METODO_PAGAMENTO,
  MODALIDADE_ENTREGA,
} from '../../../core/models/compra.model';
import { ProblemDetail } from '../../../core/models/problem.model';
import { CarrinhoService } from '../../../core/services/carrinho.service';
import { CheckoutService } from '../../../core/services/checkout.service';
import { ContaPedidoService } from '../../../core/services/conta-pedido.service';
import { ContaService } from '../../../core/services/conta.service';
import { EnderecoClienteService } from '../../../core/services/endereco-cliente.service';
import { reais } from '../../../core/utils/moeda';

/** Nomes das modalidades como no protótipo 02 (checkout.html). */
const NOME_MODALIDADE: Record<number, string> = {
  [MODALIDADE_ENTREGA.EXPRESSA]: 'Expresso',
  [MODALIDADE_ENTREGA.PADRAO]: 'Padrão',
  [MODALIDADE_ENTREGA.RETIRADA_SHOWROOM]: 'Retirar no showroom',
};

/**
 * Checkout (#26, protótipo 02 checkout.html): ao entrar reserva o estoque (#23), cota o frete
 * do endereço escolhido (#30) e confirma criando o pedido; PIX, boleto e cartão salvo
 * já abrem o pagamento (#27).
 */
@Component({
  selector: 'app-checkout',
  imports: [RouterLink],
  templateUrl: './checkout.html',
  styleUrl: './checkout.scss',
})
export class Checkout implements OnInit {
  private readonly carrinhos = inject(CarrinhoService);
  private readonly checkout = inject(CheckoutService);
  private readonly enderecosService = inject(EnderecoClienteService);
  private readonly conta = inject(ContaService);
  private readonly pedidos = inject(ContaPedidoService);
  private readonly router = inject(Router);

  protected readonly reais = reais;
  protected readonly METODO = METODO_PAGAMENTO;
  protected readonly nomeModalidade = NOME_MODALIDADE;

  readonly carrinho = signal<Carrinho | null>(null);
  readonly enderecos = signal<EnderecoCliente[]>([]);
  readonly cotacoes = signal<CotacaoFrete[]>([]);
  readonly cartoes = signal<CartaoSalvo[]>([]);
  readonly idEndereco = signal<number | null>(null);
  readonly modalidade = signal<number | null>(null);
  readonly metodo = signal<number>(METODO_PAGAMENTO.PIX);
  readonly idCartao = signal<number | null>(null);
  readonly erro = signal<string | null>(null);
  readonly enviando = signal(false);

  readonly cotacao = computed(
    () => this.cotacoes().find((c) => c.modalidade.id === this.modalidade()) ?? null,
  );
  readonly frete = computed(() => this.cotacao()?.custo ?? 0);
  readonly total = computed(() => (this.carrinho()?.total ?? 0) + this.frete());

  ngOnInit(): void {
    this.carrinhos.reservar().subscribe({
      next: (c) => this.carrinho.set(c),
      error: (e: HttpErrorResponse) =>
        this.erro.set(this.texto(e, 'Não foi possível reservar os itens.')),
    });
    this.enderecosService.listar().subscribe((lista) => {
      this.enderecos.set(lista);
      const principal = lista.find((e) => e.principal) ?? lista[0];
      if (principal) this.escolherEndereco(principal);
    });
    this.conta.cartoes().subscribe({
      next: (c) => {
        this.cartoes.set(c);
        this.idCartao.set(c.find((x) => x.padrao)?.id ?? null);
      },
      error: () => this.cartoes.set([]),
    });
  }

  escolherEndereco(e: EnderecoCliente): void {
    this.idEndereco.set(e.id);
    this.checkout.cotar(e.cep).subscribe((cot) => {
      this.cotacoes.set(cot);
      const padrao = cot.find((c) => c.modalidade.id === MODALIDADE_ENTREGA.PADRAO) ?? cot[0];
      this.modalidade.set(padrao?.modalidade.id ?? null);
    });
  }

  prazo(c: CotacaoFrete): string {
    if (c.modalidade.id === MODALIDADE_ENTREGA.RETIRADA_SHOWROOM)
      return 'Palmas, Plano Diretor Sul · disponível amanhã';
    return `Receba em ${c.prazoDias} ${c.prazoDias === 1 ? 'dia útil' : 'dias úteis'}`;
  }

  linhaEndereco(e: EnderecoCliente): string {
    const comp = e.complemento ? `, ${e.complemento}` : '';
    return `${e.logradouro}, ${e.numero}${comp}`;
  }

  confirmar(): void {
    const idEndereco = this.idEndereco();
    const cot = this.cotacao();
    if (!idEndereco || !cot || this.enviando()) {
      this.erro.set('Escolha o endereço e a entrega.');
      return;
    }
    const metodo = this.metodo();
    if (metodo === METODO_PAGAMENTO.CARTAO && !this.idCartao()) {
      this.erro.set('Escolha um cartão salvo em Meios de pagamento.');
      return;
    }
    this.enviando.set(true);
    this.erro.set(null);
    this.checkout
      .confirmar({
        idEndereco,
        idModalidadeEntrega: cot.modalidade.id,
        codigoServicoTransportadora: cot.codigoServicoTransportadora,
        idMetodoPagamento: metodo,
      })
      .subscribe({
        next: (pedido) => {
          this.carrinhos.esquecer();
          const pagamento =
            metodo === METODO_PAGAMENTO.CARTAO
              ? { idMetodo: metodo, idCartaoSalvo: this.idCartao() }
              : { idMetodo: metodo };
          // o pedido já existe: falha no pagamento não impede de seguir (paga depois em Meus pedidos)
          this.pedidos.pagar(pedido.id, pagamento).subscribe({
            complete: () => void this.router.navigate(['/pedido-confirmado', pedido.id]),
            error: () => void this.router.navigate(['/pedido-confirmado', pedido.id]),
          });
        },
        error: (e: HttpErrorResponse) => {
          this.enviando.set(false);
          this.erro.set(this.texto(e, 'Não foi possível confirmar o pedido.'));
        },
      });
  }

  private texto(e: HttpErrorResponse, padrao: string): string {
    const p = e.error as ProblemDetail | null;
    return p?.errors?.[0]?.message ?? p?.detail ?? padrao;
  }
}
