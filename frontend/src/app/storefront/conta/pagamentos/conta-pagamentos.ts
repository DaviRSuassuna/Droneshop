import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, OnInit, inject, signal, viewChild } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CartaoSalvo } from '../../../core/models/compra.model';
import { ProblemDetail } from '../../../core/models/problem.model';
import { ContaService } from '../../../core/services/conta.service';
import { AccountMenu } from '../../../layout/storefront/account-menu/account-menu';
import { ContaSessao } from '../conta-sessao';

/**
 * "Minha conta > Meios de pagamento" (#28, protótipo 02 conta-pagamentos.html).
 * O cartão é guardado só pelo token do gateway e pelos 4 últimos dígitos (RN11): o número
 * completo nunca sai do navegador.
 */
@Component({
  selector: 'app-conta-pagamentos',
  imports: [AccountMenu, RouterLink, ReactiveFormsModule],
  providers: [ContaSessao],
  templateUrl: './conta-pagamentos.html',
  styleUrl: './conta-pagamentos.scss',
})
export class ContaPagamentos implements OnInit {
  protected readonly sessao = inject(ContaSessao);
  private readonly conta = inject(ContaService);
  private readonly fb = inject(FormBuilder);
  private readonly dialogo = viewChild.required<ElementRef<HTMLDialogElement>>('dlgCartao');

  readonly cartoes = signal<CartaoSalvo[]>([]);
  readonly mensagem = signal<{ texto: string; ok: boolean } | null>(null);

  readonly form = this.fb.nonNullable.group({
    numero: ['', [Validators.required, Validators.pattern(/^[\d ]{13,23}$/)]],
    validade: ['', [Validators.required, Validators.pattern(/^\d{2}\/\d{2}$/)]],
    nome: ['', Validators.required],
  });

  ngOnInit(): void {
    this.sessao.carregar();
    this.carregar();
  }

  carregar(): void {
    this.conta.cartoes().subscribe((c) => this.cartoes.set(c));
  }

  abrir(): void {
    this.form.reset();
    this.mensagem.set(null);
    this.dialogo().nativeElement.showModal();
  }

  fechar(): void {
    this.dialogo().nativeElement.close();
  }

  bandeira(numero: string): string {
    if (numero.startsWith('4')) return 'visa';
    if (/^5[1-5]/.test(numero)) return 'mastercard';
    if (/^3[47]/.test(numero)) return 'amex';
    return 'cartao';
  }

  // Sandbox: o token viria do SDK do Mercado Pago; aqui é um token de teste derivado no navegador
  salvar(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
    const v = this.form.getRawValue();
    const digitos = v.numero.replace(/\D/g, '');
    this.conta
      .salvarCartao({
        tokenGateway: `card_tok_${Date.now()}`,
        ultimosDigitos: digitos.slice(-4),
        bandeira: this.bandeira(digitos),
        validade: v.validade,
      })
      .subscribe({
        next: () => {
          this.fechar();
          this.carregar();
        },
        error: (e: HttpErrorResponse) => {
          const p = e.error as ProblemDetail | null;
          this.mensagem.set({
            texto: p?.errors?.[0]?.message ?? 'Não foi possível salvar o cartão.',
            ok: false,
          });
        },
      });
  }

  padrao(c: CartaoSalvo): void {
    this.conta.tornarPadrao(c.id).subscribe(() => this.carregar());
  }

  remover(c: CartaoSalvo): void {
    this.conta.excluirCartao(c.id).subscribe(() => this.carregar());
  }
}
