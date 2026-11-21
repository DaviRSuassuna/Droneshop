import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Chamado, MensagemChamado } from '../../core/models/chamado.model';
import { PedidoListagem } from '../../core/models/pedido.model';
import { ProblemDetail } from '../../core/models/problem.model';
import { AuthService } from '../../core/services/auth.service';
import { ChamadoService } from '../../core/services/chamado.service';
import { ContaPedidoService } from '../../core/services/conta-pedido.service';

/** Central de ajuda (protótipo suporte.html): abrir chamado e acompanhar os seus (#38). */
@Component({
  selector: 'app-suporte',
  imports: [ReactiveFormsModule, RouterLink, DatePipe],
  templateUrl: './suporte.html',
  styleUrl: './suporte.scss',
})
export class Suporte implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly chamados = inject(ChamadoService);
  private readonly contaPedidos = inject(ContaPedidoService);
  private readonly auth = inject(AuthService);

  readonly assuntos = [
    'Garantia e reparo',
    'Pedido e entrega',
    'Troca ou devolução',
    'Registro ANAC',
    'Outro',
  ];

  readonly logado = signal(this.auth.autenticado());
  readonly pedidos = signal<PedidoListagem[]>([]);
  readonly meus = signal<Chamado[]>([]);
  readonly aberto = signal<Chamado | null>(null);
  readonly mensagens = signal<MensagemChamado[]>([]);
  readonly enviando = signal(false);
  readonly sucesso = signal<string | null>(null);
  readonly erro = signal<string | null>(null);
  readonly erroResposta = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    assunto: [this.assuntos[0], Validators.required],
    idPedido: [''],
    mensagem: ['', [Validators.required, Validators.maxLength(5000)]],
  });
  readonly resposta = this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(5000)]);

  ngOnInit(): void {
    if (!this.logado()) return;
    this.contaPedidos.listar(0, 20).subscribe({
      next: (r) => this.pedidos.set(r.items ?? []),
      error: () => this.pedidos.set([]),
    });
    this.carregarMeus();
  }

  carregarMeus(): void {
    this.chamados.meus().subscribe({
      next: (l) => this.meus.set(Array.isArray(l) ? l : []),
      error: () => this.meus.set([]),
    });
  }

  enviar(): void {
    this.sucesso.set(null);
    this.erro.set(null);
    if (!this.logado()) {
      this.erro.set('Entre na sua conta para abrir um chamado.');
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.erro.set('Descreva o problema para abrir o chamado.');
      return;
    }
    const v = this.form.getRawValue();
    this.enviando.set(true);
    this.chamados
      .abrir({ assunto: v.assunto, idPedido: v.idPedido ? Number(v.idPedido) : null, mensagem: v.mensagem })
      .subscribe({
        next: (c) => {
          this.enviando.set(false);
          this.sucesso.set(c?.protocolo ? `Chamado ${c.protocolo} aberto.` : 'Chamado aberto.');
          this.form.controls.mensagem.reset('');
          this.carregarMeus();
        },
        error: (e: HttpErrorResponse) => {
          this.enviando.set(false);
          const p = e.error as ProblemDetail | null;
          this.erro.set(p?.errors?.[0]?.message ?? 'Não foi possível abrir o chamado.');
        },
      });
  }

  abrir(c: Chamado): void {
    this.aberto.set(c);
    this.erroResposta.set(null);
    this.chamados.mensagens(c.id).subscribe({
      next: (m) => this.mensagens.set(m),
      error: () => this.mensagens.set([]),
    });
  }

  responder(): void {
    const c = this.aberto();
    if (!c || this.resposta.invalid) return;
    this.chamados.responder(c.id, this.resposta.value).subscribe({
      next: () => {
        this.resposta.reset('');
        this.abrir(c);
      },
      error: (e: HttpErrorResponse) =>
        this.erroResposta.set(
          e.status === 409 ? 'Este chamado foi encerrado.' : 'Não foi possível enviar a mensagem.',
        ),
    });
  }
}
