import { HttpErrorResponse } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Notificacao, TemplateEmail } from '../../core/models/notificacao.model';
import { PageResponse } from '../../core/models/page.model';
import { ProblemDetail } from '../../core/models/problem.model';
import { NotificacaoService } from '../../core/services/notificacao.service';

/** Templates de e-mail editáveis e histórico de envios (#32, RF38 a RF40). */
@Component({
  selector: 'app-templates-email',
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './templates-email.html',
  styleUrl: './templates-email.scss',
})
export class TemplatesEmailPage implements OnInit {
  private readonly service = inject(NotificacaoService);
  private readonly fb = inject(FormBuilder);

  readonly templates = signal<TemplateEmail[]>([]);
  readonly selecionado = signal<TemplateEmail | null>(null);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly erroCampo = signal<string | null>(null);
  readonly salvo = signal<string | null>(null);
  readonly salvando = signal(false);

  readonly envios = signal<PageResponse<Notificacao> | null>(null);
  readonly destinatario = signal('');
  readonly idStatus = signal<number | null>(null);
  readonly page = signal(0);

  readonly status = [
    { id: 1, nome: 'Enviada' },
    { id: 2, nome: 'Entregue' },
    { id: 3, nome: 'Aberta' },
    { id: 4, nome: 'Bounce' },
    { id: 5, nome: 'Spam' },
    { id: 6, nome: 'Falha' },
  ];

  readonly form = this.fb.nonNullable.group({
    assunto: ['', [Validators.required, Validators.maxLength(200)]],
    corpoTemplate: ['', Validators.required],
  });

  ngOnInit(): void {
    this.carregar();
    this.buscarEnvios();
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.service.templates().subscribe({
      next: (lista) => {
        this.templates.set(lista);
        this.carregando.set(false);
      },
      error: () => {
        this.erro.set('Não foi possível carregar os templates.');
        this.carregando.set(false);
      },
    });
  }

  abrir(codigo: string): void {
    this.salvo.set(null);
    this.erroCampo.set(null);
    this.service.template(codigo).subscribe((t) => {
      this.selecionado.set(t);
      this.form.setValue({ assunto: t.assunto, corpoTemplate: t.corpoTemplate ?? '' });
    });
  }

  salvar(): void {
    const t = this.selecionado();
    if (!t) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.salvando.set(true);
    this.erroCampo.set(null);
    this.service.editar(t.codigo, this.form.getRawValue()).subscribe({
      next: (nova) => {
        this.salvando.set(false);
        this.selecionado.set(nova);
        this.salvo.set(`Versão ${nova.versao} salva.`);
        this.carregar();
      },
      error: (e: HttpErrorResponse) => {
        this.salvando.set(false);
        const p = e.error as ProblemDetail | null;
        this.erroCampo.set(p?.errors?.[0]?.message ?? 'Não foi possível salvar o template.');
      },
    });
  }

  buscarEnvios(page = 0): void {
    this.page.set(page);
    this.service
      .notificacoes({
        destinatario: this.destinatario(),
        idStatusEnvio: this.idStatus(),
        page,
        pageSize: 10,
      })
      .subscribe({ next: (r) => this.envios.set(r), error: () => this.envios.set(null) });
  }

  filtrarStatus(valor: string): void {
    this.idStatus.set(valor ? Number(valor) : null);
    this.buscarEnvios();
  }
}
