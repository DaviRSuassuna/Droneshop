import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConfiguracaoLojaService } from '../../core/services/configuracao-loja.service';
import { ProblemDetail } from '../../core/models/problem.model';
import { somenteDigitos } from '../../core/utils/documentos';
import { MascaraDirective } from '../../shared/directives/mascara.directive';

@Component({
  selector: 'app-configuracao-loja',
  imports: [ReactiveFormsModule, MascaraDirective, RouterLink],
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
  readonly salvo = signal(false);

  // mensagens no mapa fieldMessages e erros do back aplicados no próprio controle como "api".
  readonly form = this.fb.nonNullable.group({
    dadosFiscais: ['', [Validators.required, Validators.maxLength(2000)]],
    cepOrigemFrete: ['', [Validators.required, Validators.pattern(/^\d{5}-\d{3}$/)]],
    remetentePadrao: ['', [Validators.required, Validators.maxLength(150)]],
    prazoArrependimentoDias: [
      7,
      [Validators.required, Validators.min(7), Validators.pattern(/^\d+$/)],
    ],
    prazoGarantiaDias: [365, [Validators.required, Validators.min(1), Validators.pattern(/^\d+$/)]],
    prazoAnaliseDevolucaoDiasUteis: [
      5,
      [Validators.required, Validators.min(1), Validators.pattern(/^\d+$/)],
    ],
    freteReversoPorContaLoja: [true],
  });

  readonly valores = toSignal(this.form.valueChanges.pipe(map(() => this.form.getRawValue())), {
    initialValue: this.form.getRawValue(),
  });

  private readonly fieldMessages: Record<string, Record<string, string>> = {
    dadosFiscais: {
      required: 'Informe os dados fiscais da loja.',
      maxlength: 'Os dados fiscais devem ter no máximo 2000 caracteres.',
    },
    cepOrigemFrete: {
      required: 'Informe o CEP de origem do frete.',
      pattern: 'CEP inválido. Use o formato 00000-000.',
    },
    remetentePadrao: {
      required: 'Informe o remetente padrão.',
      maxlength: 'O remetente padrão deve ter no máximo 150 caracteres.',
    },
    prazoArrependimentoDias: {
      required: 'Informe o prazo de arrependimento.',
      min: 'O prazo de arrependimento deve ser de pelo menos 7 dias.',
      pattern: 'Informe um número inteiro de dias.',
    },
    prazoGarantiaDias: {
      required: 'Informe o prazo de garantia.',
      min: 'O prazo de garantia deve ser de pelo menos 1 dia.',
      pattern: 'Informe um número inteiro de dias.',
    },
    prazoAnaliseDevolucaoDiasUteis: {
      required: 'Informe o prazo de análise da devolução.',
      min: 'O prazo de análise deve ser de pelo menos 1 dia útil.',
      pattern: 'Informe um número inteiro de dias.',
    },
  };

  constructor() {
    // "Configuração salva." só vale até a próxima alteração
    this.form.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => this.salvo.set(false));
  }

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.falhaCarregamento.set(false);
    this.erroGeral.set(null);
    this.service.buscar().subscribe({
      next: (configuracao) => {
        this.form.reset({
          ...configuracao,
          cepOrigemFrete: `${configuracao.cepOrigemFrete.slice(0, 5)}-${configuracao.cepOrigemFrete.slice(5)}`,
        });
        this.carregando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.carregando.set(false);
        this.falhaCarregamento.set(true);
        const problem = err.error as ProblemDetail | undefined;
        this.erroGeral.set(problem?.detail ?? 'Não foi possível carregar a configuração da loja.');
      },
    });
  }

  /** Mensagem do primeiro erro do controle, só depois que o usuário mexeu nele (ou tentou salvar). */
  resolveErrorMessage(path: string): string | null {
    const control = this.form.get(path);
    if (!control || !control.errors || !(control.touched || control.dirty)) return null;
    if (control.errors['api']) return control.errors['api'];
    const mensagens = this.fieldMessages[path] ?? {};
    for (const tipo of Object.keys(control.errors)) {
      if (mensagens[tipo]) return mensagens[tipo];
    }
    return 'Valor inválido.';
  }

  salvar(): void {
    if (this.salvando() || this.carregando() || this.falhaCarregamento()) return;
    this.erroGeral.set(null);
    this.salvo.set(false);

    // validação do front: não envia enquanto houver erro, e mostra todos de uma vez
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.erroGeral.set('Verifique os campos destacados.');
      return;
    }

    const valores = this.form.getRawValue();
    this.salvando.set(true);
    this.service
      .atualizar({
        ...valores,
        cepOrigemFrete: somenteDigitos(valores.cepOrigemFrete),
      })
      .subscribe({
        next: () => {
          this.salvando.set(false);
          this.salvo.set(true);
        },
        error: (err: HttpErrorResponse) => this.tratarErro(err),
      });
  }

  /** Erro do back (Problem, RFC 7807): cada erro de campo vai para o controle certo como "api". */
  private tratarErro(err: HttpErrorResponse): void {
    this.salvando.set(false);
    const problem = err.error as ProblemDetail | undefined;
    if (problem?.errors?.length) {
      for (const fe of problem.errors) {
        this.aplicarErroApi(fe.field, fe.message);
      }
      // o detalhe de cada erro já aparece embaixo do campo
      this.erroGeral.set('Verifique os campos destacados.');
    } else {
      this.erroGeral.set(
        problem?.detail ?? 'Não foi possível salvar a configuração. Tente novamente.',
      );
    }
  }

  private aplicarErroApi(path: string, mensagem: string): void {
    const control = this.form.get(path);
    if (!control) {
      this.erroGeral.set(mensagem);
      return;
    }
    control.setErrors({ ...(control.errors ?? {}), api: mensagem });
    control.markAsTouched();
  }
}
