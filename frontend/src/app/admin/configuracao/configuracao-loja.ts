import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminNav } from '../../layout/admin/admin-nav/admin-nav';
import { ConfiguracaoLojaService } from '../../core/services/configuracao-loja.service';
import { ProblemDetail } from '../../core/models/problem.model';
import { somenteDigitos } from '../../core/utils/documentos';
import { MascaraDirective } from '../../shared/directives/mascara.directive';

@Component({
  selector: 'app-configuracao-loja',
  imports: [ReactiveFormsModule, AdminNav, MascaraDirective],
  templateUrl: './configuracao-loja.html',
  styleUrl: './configuracao-loja.scss',
})
export class ConfiguracaoLojaPage implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ConfiguracaoLojaService);

  readonly carregando = signal(true);
  readonly falhaCarregamento = signal(false);
  readonly salvando = signal(false);
  readonly erroGeral = signal<string | null>(null);
  readonly errosCampo = signal<Record<string, string>>({});
  readonly salvo = signal(false);

  readonly form = this.fb.nonNullable.group({
    dadosFiscais: ['', [Validators.required, Validators.maxLength(2000)]],
    cepOrigemFrete: ['', [Validators.required, Validators.pattern(/^\d{5}-\d{3}$/)]],
    remetentePadrao: ['', [Validators.required, Validators.maxLength(150)]],
    prazoArrependimentoDias: [7, [Validators.required, Validators.min(7), Validators.pattern(/^\d+$/)]],
    prazoGarantiaDias: [365, [Validators.required, Validators.min(1), Validators.pattern(/^\d+$/)]],
    prazoAnaliseDevolucaoDiasUteis: [5, [Validators.required, Validators.min(1), Validators.pattern(/^\d+$/)]],
    freteReversoPorContaLoja: [true],
  });

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.falhaCarregamento.set(false);
    this.erroGeral.set(null);
    this.service.buscar().subscribe({
      next: (configuracao) => {
        this.form.patchValue({
          ...configuracao,
          cepOrigemFrete: `${configuracao.cepOrigemFrete.slice(0, 5)}-${configuracao.cepOrigemFrete.slice(5)}`,
        });
        this.carregando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.carregando.set(false);
        this.falhaCarregamento.set(true);
        this.erroGeral.set(this.detalheProblema(err) ?? 'Não foi possível carregar a configuração da loja.');
      },
    });
  }

  salvar(): void {
    this.erroGeral.set(null);
    this.errosCampo.set({});
    this.salvo.set(false);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valores = this.form.getRawValue();
    this.salvando.set(true);
    this.service.atualizar({
      ...valores,
      cepOrigemFrete: somenteDigitos(valores.cepOrigemFrete),
    }).subscribe({
      next: () => {
        this.salvando.set(false);
        this.salvo.set(true);
      },
      error: (err: HttpErrorResponse) => {
        this.salvando.set(false);
        const problem = err.error as ProblemDetail | undefined;
        if (problem?.errors?.length) {
          this.errosCampo.set(Object.fromEntries(problem.errors.map(({ field, message }) => [field, message])));
        }
        this.erroGeral.set(this.detalheProblema(err) ?? 'Não foi possível salvar a configuração. Tente novamente.');
      },
    });
  }

  mensagemErro(campo: keyof typeof this.form.controls): string | null {
    const api = this.errosCampo()[campo];
    if (api) return api;
    const control = this.form.controls[campo];
    if (!control.touched || control.valid) return null;

    if (control.hasError('required')) return 'Este campo é obrigatório.';
    if (control.hasError('pattern')) {
      return campo === 'cepOrigemFrete'
        ? 'Informe um CEP válido no formato 00000-000.'
        : 'Informe um número inteiro de dias.';
    }
    if (control.hasError('min')) {
      const minimo = campo === 'prazoArrependimentoDias' ? 7 : 1;
      return `Informe pelo menos ${minimo} ${campo === 'prazoArrependimentoDias' ? 'dias' : 'dia'}.`;
    }
    if (control.hasError('maxlength')) return 'O texto ultrapassou o limite de caracteres.';
    return null;
  }

  private detalheProblema(err: HttpErrorResponse): string | null {
    const problem = err.error as ProblemDetail | undefined;
    return problem?.detail ?? null;
  }
}
