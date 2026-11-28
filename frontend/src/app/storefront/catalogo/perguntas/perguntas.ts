import { HttpErrorResponse } from '@angular/common/http';
import { Component, effect, inject, input, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ProblemDetail } from '../../../core/models/problem.model';
import { AuthService } from '../../../core/services/auth.service';
import { Pergunta, PerguntaService } from '../../../core/services/pergunta.service';

/** Seção "Perguntas" da página do produto (#40): só aparecem as aprovadas (RN05). */
@Component({
  selector: 'app-perguntas',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './perguntas.html',
})
export class Perguntas {
  private readonly service = inject(PerguntaService);
  private readonly auth = inject(AuthService);

  readonly slug = input.required<string>();

  readonly perguntas = signal<Pergunta[]>([]);
  readonly logado = signal(this.auth.temPapel('CLIENTE'));
  readonly aviso = signal<string | null>(null);
  readonly respondendo = signal<number | null>(null);

  readonly texto = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.minLength(10), Validators.maxLength(1000)],
  });
  readonly resposta = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(2000)],
  });

  constructor() {
    effect(() => this.carregar(this.slug()));
  }

  carregar(slug: string): void {
    this.service.aprovadas(slug).subscribe({
      next: (l) => this.perguntas.set(Array.isArray(l) ? l : []),
      error: () => this.perguntas.set([]),
    });
  }

  perguntar(): void {
    if (this.texto.invalid) {
      this.texto.markAsTouched();
      this.aviso.set('A pergunta precisa ter ao menos 10 caracteres.');
      return;
    }
    this.service.perguntar(this.slug(), this.texto.value).subscribe({
      next: () => {
        this.texto.reset('');
        this.aviso.set('Pergunta enviada. Ela aparece aqui depois da moderação.');
      },
      error: (e: HttpErrorResponse) => {
        const p = e.error as ProblemDetail | null;
        this.aviso.set(p?.errors?.[0]?.message ?? 'Não foi possível enviar a pergunta.');
      },
    });
  }

  responder(id: number): void {
    if (this.resposta.invalid) return;
    this.service.responder(id, this.resposta.value).subscribe({
      next: () => {
        this.resposta.reset('');
        this.respondendo.set(null);
        this.carregar(this.slug());
      },
      error: () => this.aviso.set('Não foi possível enviar a resposta.'),
    });
  }
}
