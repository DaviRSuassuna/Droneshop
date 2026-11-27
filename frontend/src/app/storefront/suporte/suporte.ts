import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Chamado, MensagemChamado } from '../../core/models/chamado.model';
import { PedidoListagem } from '../../core/models/pedido.model';
import { ProblemDetail } from '../../core/models/problem.model';
import { AuthService } from '../../core/services/auth.service';
import { ChamadoService } from '../../core/services/chamado.service';
import { ContaPedidoService } from '../../core/services/conta-pedido.service';
import { LojaService } from '../../core/services/loja.service';
import { PRAZOS_PADRAO, PrazosLoja, RAPIDAS, TEMAS, TemaSuporte } from './suporte.conteudo';

/**
 * Central de ajuda (protótipo suporte.html): temas e perguntas frequentes com texto fixo (#90),
 * abrir chamado e acompanhar os seus (#38).
 */
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
  private readonly loja = inject(LojaService);

  readonly temas = TEMAS;
  readonly prazos = signal<PrazosLoja>(PRAZOS_PADRAO);
  readonly temaAtivo = signal<TemaSuporte | null>(null);
  readonly busca = signal('');

  /** Perguntas visíveis: do tema escolhido, da busca ou as "Respostas rápidas". */
  readonly perguntas = computed(() => {
    const todas = this.temas.flatMap((t) => t.perguntas);
    const q = this.normalizar(this.busca());
    if (q) return todas.filter((p) => this.normalizar(p.pergunta + ' ' + p.resposta(this.prazos())).includes(q));
    const tema = this.temaAtivo();
    if (tema) return tema.perguntas;
    return RAPIDAS.map((r) => todas.find((p) => p.pergunta === r)!).filter(Boolean);
  });

  readonly assuntos = [
    'Garantia e reparo',
    'Pedido e entrega',
    'Troca ou devolução',
    'Registro ANAC',
    'Outro',
  ];

  readonly logado = signal(this.auth.temPapel('CLIENTE'));
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
    this.loja.buscarConfiguracao().subscribe({
      next: (c) => {
        if (c && typeof c.prazoArrependimentoDias === 'number')
          this.prazos.set({
            prazoArrependimentoDias: c.prazoArrependimentoDias,
            freteReversoPorContaLoja: !!c.freteReversoPorContaLoja,
          });
      },
      error: () => this.prazos.set(PRAZOS_PADRAO),
    });
    if (!this.logado()) return;
    this.contaPedidos.listar(0, 20).subscribe({
      next: (r) => this.pedidos.set(r.items ?? []),
      error: () => this.pedidos.set([]),
    });
    this.carregarMeus();
  }

  /** "Ver artigos": abre as perguntas do tema na própria página. */
  verTema(tema: TemaSuporte): void {
    this.busca.set('');
    this.temaAtivo.set(this.temaAtivo()?.id === tema.id ? null : tema);
    document.getElementById('faq')?.scrollIntoView({ block: 'start' });
  }

  buscar(texto: string): void {
    this.temaAtivo.set(null);
    this.busca.set(texto);
  }

  private normalizar(s: string): string {
    return s.normalize('NFD').replace(/\p{M}/gu, '').toLowerCase().trim();
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
