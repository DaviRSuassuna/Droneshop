import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AdminNav } from '../../../layout/admin/admin-nav/admin-nav';
import { EstoqueService } from '../../../core/services/estoque.service';
import { FornecedorService } from '../../../core/services/fornecedor.service';
import {
  EXIBICAO_TIPO_MOVIMENTO,
  ItemEstoque,
  MovimentoEstoque,
  TIPO_MOVIMENTO,
} from '../../../core/models/estoque.model';
import { Fornecedor } from '../../../core/models/fornecedor.model';
import { ProblemDetail } from '../../../core/models/problem.model';

/** Variantes do select: todas de uma vez. */
const LIMITE_VARIANTES = 1000;
const ULTIMOS_MOVIMENTOS = 5;
const MAX_OBSERVACAO = 500;

@Component({
  selector: 'app-estoque-movimento',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, AdminNav, DatePipe],
  templateUrl: './estoque-movimento.html',
  styleUrl: './estoque-movimento.scss',
})
export class EstoqueMovimento implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly estoqueService = inject(EstoqueService);
  private readonly fornecedorService = inject(FornecedorService);

  readonly tipos = Object.values(TIPO_MOVIMENTO).map((id) => ({
    id,
    rotulo: `${EXIBICAO_TIPO_MOVIMENTO[id].rotulo} · ${EXIBICAO_TIPO_MOVIMENTO[id].descricao}`,
  }));

  readonly variantes = signal<ItemEstoque[]>([]);
  readonly fornecedores = signal<Fornecedor[]>([]);
  readonly ultimos = signal<MovimentoEstoque[]>([]);
  readonly carregandoUltimos = signal(false);
  readonly salvando = signal(false);
  readonly erroGeral = signal<string | null>(null);
  readonly sucesso = signal<string | null>(null);

  // mensagens no mapa fieldMessages e erros do back aplicados no próprio controle como "api"
  readonly form = this.fb.group(
    {
      idVariante: this.fb.control<number | null>(null, Validators.required),
      idTipo: this.fb.nonNullable.control<number>(TIPO_MOVIMENTO.ENTRADA, Validators.required),
      quantidade: ['', [Validators.required, Validators.pattern(/^\d+$/)]],
      notaFiscal: ['', Validators.maxLength(30)],
      idFornecedor: this.fb.control<number | null>(null),
      observacao: ['', Validators.maxLength(MAX_OBSERVACAO)],
    },
    { validators: (g) => this.validarMovimento(g) },
  );

  private readonly fieldMessages: Record<string, Record<string, string>> = {
    idVariante: { required: 'Selecione a variante.' },
    idTipo: { required: 'Selecione o tipo do movimento.' },
    quantidade: {
      required: 'Informe a quantidade.',
      pattern: 'Informe um número inteiro, sem sinal.',
      min: 'A quantidade deve ser de pelo menos 1.',
      saldo: 'Saldo insuficiente para esta saída.',
    },
    notaFiscal: { maxlength: 'A nota fiscal deve ter no máximo 30 caracteres.' },
    observacao: {
      required: 'Justifique o ajuste ou a perda.',
      maxlength: `A observação deve ter no máximo ${MAX_OBSERVACAO} caracteres.`,
      tamanho: `Nota fiscal, fornecedor e observação somam mais de ${MAX_OBSERVACAO} caracteres.`,
    },
  };

  private readonly valores = toSignal(this.form.valueChanges, {
    initialValue: this.form.getRawValue(),
  });

  readonly varianteSelecionada = computed(() => {
    const id = this.valores().idVariante;
    return this.variantes().find((v) => v.variante.id === id) ?? null;
  });
  readonly tipoAtual = computed(() => Number(this.valores().idTipo));
  readonly ehAjuste = computed(() => this.tipoAtual() === TIPO_MOVIMENTO.AJUSTE);
  readonly observacaoObrigatoria = computed(
    () => this.tipoAtual() === TIPO_MOVIMENTO.AJUSTE || this.tipoAtual() === TIPO_MOVIMENTO.PERDA,
  );
  readonly saldoApos = computed(() => {
    const v = this.varianteSelecionada();
    const texto = String(this.valores().quantidade ?? '').trim();
    if (!v || !/^\d+$/.test(texto)) return null;
    return this.calcularSaldo(v.estoqueFisico ?? 0, this.tipoAtual(), Number(texto));
  });
  readonly pendencias = computed(() => {
    const v = this.valores();
    const faltando: string[] = [];
    if (!v.idVariante) faltando.push('Variante');
    if (!String(v.quantidade ?? '').trim()) faltando.push('Quantidade');
    if (this.observacaoObrigatoria() && !String(v.observacao ?? '').trim())
      faltando.push('Observação');
    return faltando;
  });

  constructor() {
    this.form.controls.idVariante.valueChanges
      .pipe(takeUntilDestroyed())
      .subscribe((id) => this.carregarUltimos(id));
    // nota fiscal e fornecedor só fazem sentido na entrada de mercadoria
    this.form.controls.idTipo.valueChanges.pipe(takeUntilDestroyed()).subscribe((tipo) => {
      if (Number(tipo) === TIPO_MOVIMENTO.ENTRADA) {
        this.form.controls.notaFiscal.enable({ emitEvent: false });
        this.form.controls.idFornecedor.enable({ emitEvent: false });
      } else {
        this.form.controls.notaFiscal.reset('', { emitEvent: false });
        this.form.controls.idFornecedor.reset(null, { emitEvent: false });
        this.form.controls.notaFiscal.disable({ emitEvent: false });
        this.form.controls.idFornecedor.disable({ emitEvent: false });
      }
    });
    this.form.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => this.sucesso.set(null));
  }

  ngOnInit(): void {
    this.carregarVariantes(Number(this.route.snapshot.queryParamMap.get('idVariante')));
    // fornecedor é do módulo de catálogo: sem permissão nele, o select fica só com "Não se aplica"
    this.fornecedorService.listar(0, 100).subscribe({
      next: (res) => this.fornecedores.set(res.items),
      error: () => this.fornecedores.set([]),
    });
  }

  rotuloVariante(v: ItemEstoque): string {
    return [
      v.produto.nome ?? 'Produto sem nome',
      v.variante.nome ?? 'Variante sem nome',
      v.variante.sku ?? '',
    ]
      .filter((p) => p)
      .join(' · ');
  }

  rotuloTipo(m: MovimentoEstoque): string {
    const rotulo = EXIBICAO_TIPO_MOVIMENTO[m.tipo.id]?.rotulo ?? m.tipo.nome;
    return m.observacao ? `${rotulo} · ${m.observacao}` : rotulo;
  }

  /** Variação do saldo com sinal; no ajuste a API já devolve a diferença. */
  variacao(m: MovimentoEstoque): string {
    const saida = m.tipo.id === TIPO_MOVIMENTO.SAIDA || m.tipo.id === TIPO_MOVIMENTO.PERDA;
    const valor = saida ? -m.quantidade : m.quantidade;
    return valor > 0 ? `+${valor}` : `${valor}`;
  }

  autor(m: MovimentoEstoque): string {
    if (m.autor) return m.autor.nome;
    return m.origem.nome === 'INTEGRACAO' ? 'Integração' : 'Sistema';
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
    this.erroGeral.set(null);
    this.sucesso.set(null);

    // validação do front: não envia enquanto houver erro, e mostra todos de uma vez
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.erroGeral.set('Verifique os campos destacados.');
      return;
    }

    const v = this.form.getRawValue();
    this.salvando.set(true);
    this.estoqueService
      .registrarMovimento({
        idVariante: v.idVariante as number,
        idTipo: Number(v.idTipo),
        quantidade: Number(v.quantidade),
        observacao: this.montarObservacao(),
      })
      .subscribe({
        next: (m) => this.aposLancar(m),
        error: (err: HttpErrorResponse) => this.tratarErro(err),
      });
  }

  private aposLancar(m: MovimentoEstoque): void {
    this.salvando.set(false);
    // o saldo novo vale para o painel e para a próxima conta de "saldo após"
    this.variantes.update((lista) =>
      lista.map((i) =>
        i.variante.id === m.variante.id
          ? { ...i, estoqueFisico: m.saldoResultante, disponivel: m.saldoResultante - i.reservado }
          : i,
      ),
    );
    this.form.patchValue({ quantidade: '', notaFiscal: '', idFornecedor: null, observacao: '' });
    this.form.markAsUntouched();
    this.carregarUltimos(m.variante.id);
    this.sucesso.set(`Movimento lançado. Saldo da variante: ${m.saldoResultante}.`);
  }

  /** NF e fornecedor não têm campo próprio no movimento: vão no início da observação. */
  private montarObservacao(g: AbstractControl = this.form): string | null {
    const texto = (campo: string) => String(g.get(campo)?.value ?? '').trim();
    const fornecedor = this.fornecedores().find((f) => f.id === g.get('idFornecedor')?.value);
    const partes = [
      texto('notaFiscal') ? `NF ${texto('notaFiscal')}` : '',
      fornecedor ? fornecedor.razaoSocial : '',
      texto('observacao'),
    ].filter((p) => p);
    return partes.length ? partes.join(' · ') : null;
  }

  /** Kits ficam de fora: não recebem movimento (D7). idInicial vem da linha clicada no estoque. */
  private carregarVariantes(idInicial?: number): void {
    this.estoqueService.listar({}, 0, LIMITE_VARIANTES).subscribe({
      next: (res) => {
        this.variantes.set(res.items.filter((i) => !i.variante.ehKit));
        if (idInicial && this.variantes().some((v) => v.variante.id === idInicial)) {
          this.form.controls.idVariante.setValue(idInicial);
        }
      },
      error: () => this.erroGeral.set('Não foi possível carregar as variantes.'),
    });
  }

  private carregarUltimos(idVariante: number | null): void {
    this.ultimos.set([]);
    if (!idVariante) return;
    this.carregandoUltimos.set(true);
    this.estoqueService.listarMovimentos({ idVariante }, 0, ULTIMOS_MOVIMENTOS).subscribe({
      next: (res) => {
        this.ultimos.set(res.items);
        this.carregandoUltimos.set(false);
      },
      error: () => this.carregandoUltimos.set(false),
    });
  }

  private calcularSaldo(atual: number, tipo: number, quantidade: number): number {
    if (tipo === TIPO_MOVIMENTO.AJUSTE) return quantidade;
    if (tipo === TIPO_MOVIMENTO.SAIDA || tipo === TIPO_MOVIMENTO.PERDA) return atual - quantidade;
    return atual + quantidade;
  }

  // Regras que dependem do tipo: AJUSTE aceita 0; saída não passa do saldo; observação
  // obrigatória em ajuste e perda. O erro fica no próprio controle, como os do back.
  private validarMovimento(g: AbstractControl): ValidationErrors | null {
    const quantidade = g.get('quantidade');
    const observacao = g.get('observacao');
    if (!quantidade || !observacao) return null;
    const tipo = Number(g.get('idTipo')?.value);

    this.trocarErro(quantidade, 'min', false);
    this.trocarErro(quantidade, 'saldo', false);
    const texto = String(quantidade.value ?? '').trim();
    if (/^\d+$/.test(texto)) {
      const q = Number(texto);
      this.trocarErro(quantidade, 'min', tipo !== TIPO_MOVIMENTO.AJUSTE && q < 1);
      const item = this.variantes().find((v) => v.variante.id === g.get('idVariante')?.value);
      if (item && q >= 1)
        this.trocarErro(
          quantidade,
          'saldo',
          this.calcularSaldo(item.estoqueFisico ?? 0, tipo, q) < 0,
        );
    }

    const obrigatoria = tipo === TIPO_MOVIMENTO.AJUSTE || tipo === TIPO_MOVIMENTO.PERDA;
    this.trocarErro(observacao, 'required', obrigatoria && !String(observacao.value ?? '').trim());
    const obs = String(observacao.value ?? '').trim().length;
    const total = this.montarObservacao(g)?.length ?? 0;
    this.trocarErro(observacao, 'tamanho', obs <= MAX_OBSERVACAO && total > MAX_OBSERVACAO);
    return null;
  }

  private trocarErro(control: AbstractControl, chave: string, ativo: boolean): void {
    const erros = { ...(control.errors ?? {}) };
    if (ativo) erros[chave] = true;
    else delete erros[chave];
    // erro do back some quando o usuário volta a mexer no valor
    if (control.dirty) delete erros['api'];
    control.setErrors(Object.keys(erros).length ? erros : null, { emitEvent: false });
  }

  /** Erro do back (Problem, RFC 7807): cada erro de campo vai para o controle certo como "api". */
  private tratarErro(err: HttpErrorResponse): void {
    this.salvando.set(false);
    const problem = err.error as ProblemDetail | undefined;
    if (problem?.errors?.length) {
      for (const fe of problem.errors) this.aplicarErroApi(fe.field, fe.message);
      this.erroGeral.set('Verifique os campos destacados.');
    } else if (err.status === 409) {
      // saldo insuficiente: o saldo da tela estava velho; recarrega e aponta a quantidade
      this.aplicarErroApi('quantidade', problem?.detail ?? 'Saldo insuficiente.');
      this.carregarVariantes();
      this.carregarUltimos(this.form.controls.idVariante.value);
      this.erroGeral.set('Verifique os campos destacados.');
    } else {
      this.erroGeral.set(
        problem?.detail ?? 'Não foi possível lançar o movimento. Tente novamente.',
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
    control.markAsPristine();
  }
}
