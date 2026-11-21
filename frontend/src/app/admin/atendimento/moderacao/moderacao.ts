import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { PageResponse } from '../../../core/models/page.model';
import { Pergunta, PerguntaService, STATUS_MODERACAO } from '../../../core/services/pergunta.service';

/** Moderação do conteúdo dos clientes (RN05): perguntas (#40). */
@Component({
  selector: 'app-moderacao',
  imports: [DatePipe, ReactiveFormsModule],
  templateUrl: './moderacao.html',
})
export class ModeracaoPage implements OnInit {
  private readonly perguntasService = inject(PerguntaService);

  readonly STATUS = STATUS_MODERACAO;
  readonly status = [
    { id: STATUS_MODERACAO.PENDENTE, nome: 'Pendente' },
    { id: STATUS_MODERACAO.APROVADA, nome: 'Aprovada' },
    { id: STATUS_MODERACAO.REJEITADA, nome: 'Rejeitada' },
  ];

  readonly idStatus = signal<number | null>(STATUS_MODERACAO.PENDENTE);
  readonly perguntas = signal<PageResponse<Pergunta> | null>(null);
  readonly respondendo = signal<number | null>(null);
  readonly erro = signal<string | null>(null);
  readonly resposta = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(2000)],
  });

  ngOnInit(): void {
    this.carregar();
  }

  carregar(page = 0): void {
    this.erro.set(null);
    this.perguntasService.listarAdmin(this.idStatus(), page).subscribe({
      next: (r) => this.perguntas.set(r),
      error: () => this.erro.set('Não foi possível carregar as perguntas.'),
    });
  }

  filtrar(valor: string): void {
    this.idStatus.set(valor ? Number(valor) : null);
    this.carregar();
  }

  moderarPergunta(id: number, idStatus: number): void {
    this.perguntasService.moderar(id, idStatus).subscribe({
      next: () => this.carregar(this.perguntas()?.page ?? 0),
      error: () => this.erro.set('Não foi possível moderar a pergunta.'),
    });
  }

  responder(id: number): void {
    if (this.resposta.invalid) return;
    this.perguntasService.responderOficial(id, this.resposta.value).subscribe({
      next: () => {
        this.resposta.reset('');
        this.respondendo.set(null);
        this.carregar(this.perguntas()?.page ?? 0);
      },
      error: () => this.erro.set('Só pergunta aprovada recebe resposta.'),
    });
  }
}
