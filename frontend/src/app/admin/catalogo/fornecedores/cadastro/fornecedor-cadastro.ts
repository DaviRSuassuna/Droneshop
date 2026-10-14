import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable, debounceTime, distinctUntilChanged, filter, map } from 'rxjs';
import { AdminNav } from '../../../../layout/admin/admin-nav/admin-nav';
import { FornecedorService } from '../../../../core/services/fornecedor.service';
import { CepService } from '../../../../core/services/cep.service';
import { EstadoService } from '../../../../core/services/estado.service';
import { MunicipioService } from '../../../../core/services/municipio.service';
import { FornecedorDTO } from '../../../../core/models/fornecedor.model';
import { STATUS_FORNECEDOR } from '../../../../core/models/status.model';
import { CepResultado, Estado, Municipio } from '../../../../core/models/endereco.model';
import { ProblemDetail } from '../../../../core/models/problem.model';
import { cnpjValidator, somenteDigitos } from '../../../../core/utils/documentos';
import { MascaraDirective } from '../../../../shared/directives/mascara.directive';
import { formatarCnpj } from '../../../../shared/pipes/cnpj.pipe';
import { formatarCep } from '../../../../shared/pipes/cep.pipe';
import { formatarTelefone } from '../../../../shared/pipes/telefone.pipe';

/** Aviso exibido embaixo do CEP depois da busca. */
interface AvisoCep {
  tipo: 'ok' | 'warn';
  texto: string;
}

@Component({
  selector: 'app-fornecedor-cadastro',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, AdminNav, MascaraDirective],
  templateUrl: './fornecedor-cadastro.html',
  styleUrl: './fornecedor-cadastro.scss',
})
export class FornecedorCadastro implements OnInit {
  readonly STATUS = STATUS_FORNECEDOR;
  private readonly fb = inject(FormBuilder);
  private readonly fornecedorService = inject(FornecedorService);
  private readonly cepService = inject(CepService);
  private readonly estadoService = inject(EstadoService);
  private readonly municipioService = inject(MunicipioService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly fornecedorId = signal<number | null>(null);
  readonly modoEdicao = computed(() => this.fornecedorId() !== null);

  readonly carregando = signal(false);
  readonly salvando = signal(false);
  readonly excluindo = signal(false);
  readonly erroGeral = signal<string | null>(null);

  readonly estados = signal<Estado[]>([]);
  readonly municipios = signal<Municipio[]>([]);
  readonly carregandoMunicipios = signal(false);
  readonly buscandoCep = signal(false);
  readonly avisoCep = signal<AvisoCep | null>(null);

  // Validações do front (padrão do código do professor): Validators em cada campo,
  // mensagens no mapa fieldMessages e erros do back aplicados no próprio controle como "api".
  readonly form = this.fb.nonNullable.group({
    razaoSocial: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(150)]],
    cnpj: ['', [Validators.required, cnpjValidator]],
    nomeContato: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    emailContato: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
    telefoneContato: ['', [Validators.pattern(/^\(\d{2}\) \d{4,5}-\d{4}$/)]],
    idStatus: [STATUS_FORNECEDOR.ATIVO as number, Validators.required],
    endereco: this.fb.nonNullable.group({
      cep: ['', [Validators.required, Validators.pattern(/^\d{5}-\d{3}$/)]],
      logradouro: ['', [Validators.required, Validators.maxLength(150)]],
      numero: ['', [Validators.required, Validators.maxLength(10)]],
      complemento: ['', [Validators.maxLength(60)]],
      bairro: ['', [Validators.required, Validators.maxLength(80)]],
      // idEstado só filtra o select de Município; o que vai para a API é idMunicipio
      idEstado: this.fb.control<number | null>(null, Validators.required),
      idMunicipio: this.fb.control<number | null>(null, Validators.required),
    }),
  });

  /** Mensagem por erro de cada controle (caminho do controle → tipo de erro → texto). */
  private readonly fieldMessages: Record<string, Record<string, string>> = {
    razaoSocial: {
      required: 'Informe a razão social.',
      minlength: 'A razão social deve ter pelo menos 2 caracteres.',
      maxlength: 'A razão social deve ter no máximo 150 caracteres.',
    },
    cnpj: {
      required: 'Informe o CNPJ.',
      cnpj: 'CNPJ inválido. Confira os dígitos.',
    },
    nomeContato: {
      required: 'Informe o nome do contato.',
      minlength: 'O nome deve ter pelo menos 2 caracteres.',
      maxlength: 'O nome deve ter no máximo 100 caracteres.',
    },
    emailContato: {
      required: 'Informe o e-mail do contato.',
      email: 'E-mail inválido.',
      maxlength: 'O e-mail deve ter no máximo 150 caracteres.',
    },
    telefoneContato: {
      pattern: 'Telefone inválido. Use (00) 0000-0000 ou (00) 00000-0000.',
    },
    idStatus: { required: 'Selecione o status.' },
    'endereco.cep': {
      required: 'Informe o CEP.',
      pattern: 'CEP inválido. Use o formato 00000-000.',
    },
    'endereco.logradouro': {
      required: 'Informe o logradouro.',
      maxlength: 'O logradouro deve ter no máximo 150 caracteres.',
    },
    'endereco.numero': {
      required: 'Informe o número (use S/N se não houver).',
      maxlength: 'O número deve ter no máximo 10 caracteres.',
    },
    'endereco.complemento': {
      maxlength: 'O complemento deve ter no máximo 60 caracteres.',
    },
    'endereco.bairro': {
      required: 'Informe o bairro.',
      maxlength: 'O bairro deve ter no máximo 80 caracteres.',
    },
    'endereco.idEstado': { required: 'Selecione o estado.' },
    'endereco.idMunicipio': { required: 'Selecione o município.' },
  };

  /** Campo que o back devolve no Problem → caminho do controle no form. */
  private readonly camposApi: Record<string, string> = {
    cep: 'endereco.cep',
    logradouro: 'endereco.logradouro',
    numero: 'endereco.numero',
    complemento: 'endereco.complemento',
    bairro: 'endereco.bairro',
    idMunicipio: 'endereco.idMunicipio',
    endereco: 'endereco.cep',
  };

  // valueChanges em signal, para a lista de pendências atualizar enquanto o usuário digita
  private readonly valores = toSignal(this.form.valueChanges.pipe(map(() => this.form.getRawValue())), {
    initialValue: this.form.getRawValue(),
  });

  // Fornecedor não tem RASCUNHO: os obrigatórios valem para qualquer status.
  readonly pendencias = computed(() => {
    const v = this.valores();
    const e = v.endereco;
    const faltando: string[] = [];
    if (!v.razaoSocial.trim()) faltando.push('Razão social');
    if (!v.cnpj.trim()) faltando.push('CNPJ');
    if (!v.nomeContato.trim()) faltando.push('Nome do contato');
    if (!v.emailContato.trim()) faltando.push('E-mail do contato');
    if (!e.cep.trim()) faltando.push('CEP');
    if (!e.logradouro.trim()) faltando.push('Logradouro');
    if (!e.numero.trim()) faltando.push('Número');
    if (!e.bairro.trim()) faltando.push('Bairro');
    if (!e.idMunicipio) faltando.push('Município');
    return faltando;
  });

  ngOnInit(): void {
    this.estadoService.listar().subscribe({
      next: (pagina) => this.estados.set(pagina.items),
      error: () => this.erroGeral.set('Não foi possível carregar a lista de estados.'),
    });

    const endereco = this.form.controls.endereco.controls;

    // troca manual de Estado: limpa o Município e recarrega a lista
    endereco.idEstado.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((idEstado) => {
        endereco.idMunicipio.setValue(null);
        this.carregarMunicipios(idEstado ? Number(idEstado) : null);
      });

    // busca automática quando o CEP completa 8 dígitos
    endereco.cep.valueChanges
      .pipe(
        map((cep) => somenteDigitos(cep)),
        debounceTime(300),
        distinctUntilChanged(),
        filter((cep) => cep.length === 8),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((cep) => this.buscarCep(cep));

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = Number(idParam);
      this.fornecedorId.set(id);
      this.carregarFornecedor(id);
    }
  }

  private carregarFornecedor(id: number): void {
    this.carregando.set(true);
    this.fornecedorService.buscarPorId(id).subscribe({
      next: (fornecedor) => {
        const end = fornecedor.endereco;
        // emitEvent: false para não disparar a busca de CEP nem limpar o Município
        this.form.patchValue(
          {
            razaoSocial: fornecedor.razaoSocial,
            cnpj: formatarCnpj(fornecedor.cnpj),
            nomeContato: fornecedor.nomeContato,
            emailContato: fornecedor.emailContato,
            telefoneContato: formatarTelefone(fornecedor.telefoneContato),
            idStatus: fornecedor.status.id,
            endereco: end
              ? {
                  cep: formatarCep(end.cep),
                  logradouro: end.logradouro,
                  numero: end.numero,
                  complemento: end.complemento ?? '',
                  bairro: end.bairro,
                  idEstado: end.municipio.estado.id,
                  idMunicipio: end.municipio.id,
                }
              : {},
          },
          { emitEvent: false },
        );
        if (end) this.carregarMunicipios(end.municipio.estado.id);
        this.form.updateValueAndValidity();
        this.carregando.set(false);
      },
      error: (err) => {
        this.erroGeral.set(err.status === 404 ? 'Este fornecedor não foi encontrado.' : 'Não foi possível carregar este fornecedor.');
        this.carregando.set(false);
      },
    });
  }

  private carregarMunicipios(idEstado: number | null, idMunicipioSelecionar?: number): void {
    this.municipios.set([]);
    if (!idEstado) return;
    this.carregandoMunicipios.set(true);
    this.municipioService.listarPorEstado(idEstado).subscribe({
      next: (pagina) => {
        this.municipios.set(pagina.items);
        if (idMunicipioSelecionar) {
          this.form.controls.endereco.controls.idMunicipio.setValue(idMunicipioSelecionar);
        }
        this.carregandoMunicipios.set(false);
      },
      error: () => {
        this.carregandoMunicipios.set(false);
        this.erroGeral.set('Não foi possível carregar os municípios.');
      },
    });
  }

  /** Botão "Buscar" e disparo automático. Nenhum resultado bloqueia o preenchimento manual. */
  buscarCep(cepInformado?: string): void {
    const cep = cepInformado ?? somenteDigitos(this.form.controls.endereco.controls.cep.value);
    if (cep.length !== 8) {
      this.form.controls.endereco.controls.cep.markAsTouched();
      this.avisoCep.set({ tipo: 'warn', texto: 'Digite os 8 dígitos do CEP para buscar.' });
      return;
    }
    this.buscandoCep.set(true);
    this.avisoCep.set(null);
    this.cepService.buscar(cep).subscribe({
      next: (resultado) => this.aplicarCep(resultado),
      error: (err: HttpErrorResponse) => this.tratarErroCep(err),
    });
  }

  private aplicarCep(resultado: CepResultado): void {
    this.buscandoCep.set(false);
    const endereco = this.form.controls.endereco;
    // número e complemento nunca são sobrescritos pela busca
    endereco.patchValue({
      logradouro: resultado.logradouro ?? '',
      bairro: resultado.bairro ?? '',
    });

    if (resultado.municipio) {
      const m = resultado.municipio;
      endereco.controls.idEstado.setValue(m.estado.id, { emitEvent: false });
      this.carregarMunicipios(m.estado.id, m.id);
      this.avisoCep.set({
        tipo: 'ok',
        texto: resultado.logradouro
          ? `Endereço encontrado: ${m.nome}/${m.estado.sigla}. Confira e informe o número.`
          : `CEP geral de ${m.nome}/${m.estado.sigla}: informe logradouro e bairro.`,
      });
    } else {
      endereco.controls.idEstado.setValue(null, { emitEvent: false });
      endereco.controls.idMunicipio.setValue(null);
      this.municipios.set([]);
      this.avisoCep.set({
        tipo: 'warn',
        texto: `Cidade "${resultado.localidade ?? ''}/${resultado.uf ?? ''}" não localizada. Selecione Estado e Município.`,
      });
    }
    queueMicrotask(() => document.getElementById('end-numero')?.focus());
  }

  private tratarErroCep(err: HttpErrorResponse): void {
    this.buscandoCep.set(false);
    const problem = err.error as ProblemDetail | undefined;
    if (err.status === 400) {
      this.aplicarErroApi('endereco.cep', problem?.errors?.[0]?.message ?? 'CEP inválido.');
      return;
    }
    // 404 ou 503: limpa o que veio de um CEP anterior e libera o preenchimento manual
    const endereco = this.form.controls.endereco;
    endereco.patchValue({ logradouro: '', bairro: '' });
    endereco.controls.idEstado.setValue(null, { emitEvent: false });
    endereco.controls.idMunicipio.setValue(null);
    this.municipios.set([]);
    this.avisoCep.set({
      tipo: 'warn',
      texto:
        err.status === 404
          ? 'CEP não encontrado. Confira o número ou preencha o endereço manualmente.'
          : 'Busca de CEP indisponível no momento. Preencha o endereço manualmente.',
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

  invalido(path: string): boolean {
    return this.resolveErrorMessage(path) !== null;
  }

  salvar(): void {
    this.erroGeral.set(null);

    // validação do front: não envia enquanto houver erro, e mostra todos de uma vez
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.erroGeral.set('Verifique os campos destacados.');
      return;
    }

    const v = this.form.getRawValue();
    const dto: FornecedorDTO = {
      razaoSocial: v.razaoSocial.trim(),
      // CNPJ, telefone e CEP vão só com dígitos; a máscara fica só na tela
      cnpj: somenteDigitos(v.cnpj),
      nomeContato: v.nomeContato.trim(),
      emailContato: v.emailContato.trim(),
      telefoneContato: somenteDigitos(v.telefoneContato),
      endereco: {
        cep: somenteDigitos(v.endereco.cep),
        logradouro: v.endereco.logradouro.trim(),
        numero: v.endereco.numero.trim(),
        complemento: v.endereco.complemento.trim(),
        bairro: v.endereco.bairro.trim(),
        idMunicipio: v.endereco.idMunicipio ? Number(v.endereco.idMunicipio) : null,
      },
      idStatus: Number(v.idStatus),
    };

    this.salvando.set(true);
    const id = this.fornecedorId();
    const request$: Observable<unknown> = id
      ? this.fornecedorService.atualizar(id, dto)
      : this.fornecedorService.criar(dto);

    request$.subscribe({
      next: () => {
        this.salvando.set(false);
        this.router.navigate(['/admin/fornecedores']);
      },
      error: (err: HttpErrorResponse) => this.tratarErro(err),
    });
  }

  excluir(): void {
    const id = this.fornecedorId();
    if (id === null) return;
    if (!confirm('Excluir este fornecedor? Essa ação não pode ser desfeita.')) return;

    this.excluindo.set(true);
    this.fornecedorService.excluir(id).subscribe({
      next: () => this.router.navigate(['/admin/fornecedores']),
      error: () => {
        this.excluindo.set(false);
        this.erroGeral.set(
          'Não foi possível excluir este fornecedor. Ele pode estar vinculado a produtos.',
        );
      },
    });
  }

  /** Erro do back (Problem, RFC 7807): cada erro de campo vai para o controle certo como "api". */
  private tratarErro(err: HttpErrorResponse): void {
    this.salvando.set(false);
    const problem = err.error as ProblemDetail | undefined;
    if (problem?.errors?.length) {
      for (const fe of problem.errors) {
        this.aplicarErroApi(this.camposApi[fe.field] ?? fe.field, fe.message);
      }
      // o detalhe de cada erro já aparece embaixo do campo
      this.erroGeral.set('Verifique os campos destacados.');
    } else {
      this.erroGeral.set(
        problem?.detail ?? 'Não foi possível salvar o fornecedor. Tente novamente.',
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
