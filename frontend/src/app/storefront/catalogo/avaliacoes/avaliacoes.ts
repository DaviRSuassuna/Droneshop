import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, computed, effect, inject, input, signal, viewChild } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ProblemDetail } from '../../../core/models/problem.model';
import { AuthService } from '../../../core/services/auth.service';
import { AvaliacaoService, AvaliacoesProduto } from '../../../core/services/avaliacao.service';

/** Seção "Avaliações" da página do produto (#39, protótipo produto.html#avaliacoes). */
@Component({
  selector: 'app-avaliacoes',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './avaliacoes.html',
})
export class Avaliacoes {
  private readonly service = inject(AvaliacaoService);
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  readonly slug = input.required<string>();
  readonly nome = input.required<string>();
  readonly drone = input(true);

  readonly dados = signal<AvaliacoesProduto>({ media: null, quantidade: 0, itens: [] });
  readonly logado = signal(this.auth.temPapel('CLIENTE'));
  readonly aviso = signal<string | null>(null);
  readonly notas = [5, 4, 3, 2, 1];

  private readonly dialogo = viewChild<ElementRef<HTMLDialogElement>>('dlg');

  readonly form = this.fb.nonNullable.group({
    nota: [5, [Validators.required, Validators.min(1), Validators.max(5)]],
    comentario: ['', Validators.maxLength(2000)],
  });

  /** Barras de 5 a 1 estrela: quantidade e largura relativa à maior. */
  readonly barras = computed(() => {
    const itens = this.dados().itens;
    const contagem = this.notas.map((n) => itens.filter((a) => a.nota === n).length);
    const maior = Math.max(1, ...contagem);
    return this.notas.map((n, i) => ({ nota: n, quantidade: contagem[i], largura: (contagem[i] / maior) * 100 }));
  });

  constructor() {
    effect(() => this.carregar(this.slug()));
  }

  estrelas(nota: number): string {
    const n = Math.round(nota);
    return '★'.repeat(n) + '☆'.repeat(5 - n);
  }

  media(): string {
    const m = this.dados().media;
    return m === null ? '—' : m.toLocaleString('pt-BR', { minimumFractionDigits: 1, maximumFractionDigits: 1 });
  }

  carregar(slug: string): void {
    this.service.doProduto(slug).subscribe({
      next: (d) => this.dados.set(d?.itens ? d : { media: null, quantidade: 0, itens: [] }),
      error: () => this.dados.set({ media: null, quantidade: 0, itens: [] }),
    });
  }

  abrir(): void {
    this.aviso.set(null);
    this.dialogo()?.nativeElement.showModal();
  }

  fechar(): void {
    this.dialogo()?.nativeElement.close();
  }

  enviar(): void {
    if (this.form.invalid) return;
    const v = this.form.getRawValue();
    this.service.avaliar(this.slug(), { nota: Number(v.nota), comentario: v.comentario }).subscribe({
      next: () => {
        this.aviso.set('Avaliação enviada. Ela aparece aqui depois da moderação.');
        this.form.reset({ nota: 5, comentario: '' });
      },
      error: (e: HttpErrorResponse) => {
        const p = e.error as ProblemDetail | null;
        this.aviso.set(
          e.status === 409
            ? 'Você já avaliou este produto.'
            : (p?.errors?.[0]?.message ?? 'Não foi possível enviar a avaliação.'),
        );
      },
    });
  }
}
