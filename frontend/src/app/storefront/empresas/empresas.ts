import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ProblemDetail } from '../../core/models/problem.model';
import { LeadEmpresaService } from '../../core/services/lead-empresa.service';
import { MascaraDirective } from '../../shared/directives/mascara.directive';

/** Página Empresas (protótipo empresas.html) com o formulário "Solicitar proposta" (D36). */
@Component({
  selector: 'app-empresas',
  imports: [ReactiveFormsModule, RouterLink, MascaraDirective],
  templateUrl: './empresas.html',
  styleUrl: './empresas.scss',
})
export class Empresas {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(LeadEmpresaService);

  readonly setores = [
    'Agronegócio',
    'Mapeamento e topografia',
    'Inspeção e energia',
    'Segurança pública',
    'Outro',
  ];
  /** Tamanho da frota -> quantidade estimada (limite inferior da faixa). */
  readonly frotas = [
    { rotulo: '1 a 3 drones', valor: 1 },
    { rotulo: '4 a 10 drones', valor: 4 },
    { rotulo: '11 a 50 drones', valor: 11 },
    { rotulo: 'Mais de 50', valor: 51 },
  ];

  readonly enviando = signal(false);
  readonly sucesso = signal(false);
  readonly erros = signal<Record<string, string>>({});
  readonly erroGeral = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    nomeContato: ['', [Validators.required, Validators.maxLength(120)]],
    emailContato: ['', [Validators.required, Validators.email]],
    razaoSocial: ['', [Validators.required, Validators.maxLength(150)]],
    cnpj: ['', Validators.required],
    segmento: [this.setores[0], Validators.required],
    quantidadeEstimada: [1],
    mensagem: ['', Validators.maxLength(1900)],
    demonstracao: [true],
  });

  erro(campo: string): string | null {
    return this.erros()[campo] ?? null;
  }

  enviar(): void {
    this.sucesso.set(false);
    this.erroGeral.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      const e: Record<string, string> = {};
      for (const [k, c] of Object.entries(this.form.controls)) if (c.invalid) e[k] = 'Preencha este campo corretamente.';
      this.erros.set(e);
      return;
    }
    const v = this.form.getRawValue();
    const mensagem = [v.mensagem.trim(), v.demonstracao ? 'Quero receber uma demonstração em campo.' : '']
      .filter(Boolean)
      .join('\n');
    this.enviando.set(true);
    this.service
      .enviar({
        razaoSocial: v.razaoSocial,
        cnpj: v.cnpj,
        nomeContato: v.nomeContato,
        emailContato: v.emailContato,
        telefoneContato: null,
        segmento: v.segmento,
        quantidadeEstimada: Number(v.quantidadeEstimada),
        mensagem: mensagem || null,
      })
      .subscribe({
        next: () => {
          this.enviando.set(false);
          this.sucesso.set(true);
          this.erros.set({});
          this.form.reset();
        },
        error: (e: HttpErrorResponse) => {
          this.enviando.set(false);
          const p = e.error as ProblemDetail | null;
          const porCampo: Record<string, string> = {};
          for (const f of p?.errors ?? []) porCampo[f.field] = f.message;
          this.erros.set(porCampo);
          if (!p?.errors?.length) this.erroGeral.set('Não foi possível enviar. Tente de novo.');
        },
      });
  }
}
