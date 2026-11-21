import { toSignal } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable, map, of, switchMap } from 'rxjs';
import { MarcaService } from '../../../../core/services/marca.service';
import { Marca, MarcaDTO } from '../../../../core/models/marca.model';
import { STATUS_PUBLICACAO } from '../../../../core/models/status.model';
import { ProblemDetail } from '../../../../core/models/problem.model';

const TIPOS_LOGO = ['image/png', 'image/svg+xml'];
const TAMANHO_MAXIMO_LOGO = 2 * 1024 * 1024;

@Component({
  selector: 'app-marca-cadastro',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './marca-cadastro.html',
  styleUrl: './marca-cadastro.scss',
})
export class MarcaCadastro implements OnInit {
  readonly STATUS = STATUS_PUBLICACAO;
  private readonly fb = inject(FormBuilder);
  private readonly marcaService = inject(MarcaService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly marcaId = signal<number | null>(null);
  readonly modoEdicao = computed(() => this.marcaId() !== null);

  readonly carregando = signal(false);
  readonly salvando = signal(false);
  readonly excluindo = signal(false);
  readonly erroGeral = signal<string | null>(null);
  readonly errosCampo = signal<Record<string, string>>({});

  // Logo por arquivo (#84): só sobe ao salvar, porque o upload precisa do id da marca
  readonly arquivoLogo = signal<File | null>(null);
  readonly previaArquivo = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    nome: [''],
    slug: [''],
    paisOrigem: [''],
    siteOficial: [''],
    descricao: [''],
    logoUrl: [''],
    idStatus: [STATUS_PUBLICACAO.RASCUNHO as number, Validators.required],
    destacarHome: [false],
  });

  private readonly valores = toSignal(
    this.form.valueChanges.pipe(map(() => this.form.getRawValue())),
    { initialValue: this.form.getRawValue() },
  );

  readonly pendencias = computed(() => {
    const v = this.valores();
    const faltando: string[] = [];
    if (!v.nome.trim()) faltando.push('Nome da marca');
    if (!v.paisOrigem.trim()) faltando.push('País de origem');
    if (!v.logoUrl.trim() && !this.arquivoLogo()) faltando.push('Logo da marca');
    return faltando;
  });

  readonly previaLogo = computed(() => {
    const url = this.valores().logoUrl.trim();
    return this.previaArquivo() ?? (url ? this.marcaService.urlLogo(url) : null);
  });

  constructor() {
    inject(DestroyRef).onDestroy(() => this.trocarPrevia(null));
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = Number(idParam);
      this.marcaId.set(id);
      this.carregarMarca(id);
    }
  }

  private carregarMarca(id: number): void {
    this.carregando.set(true);
    this.marcaService.buscarPorId(id).subscribe({
      next: (marca) => {
        this.form.patchValue({
          nome: marca.nome,
          slug: marca.slug,
          paisOrigem: marca.paisOrigem,
          siteOficial: marca.siteOficial ?? '',
          descricao: marca.descricao ?? '',
          logoUrl: marca.logoUrl,
          idStatus: marca.status.id,
          destacarHome: marca.destacarHome,
        });
        this.carregando.set(false);
      },
      error: (err) => {
        this.erroGeral.set(
          err.status === 404
            ? 'Esta marca não foi encontrada.'
            : 'Não foi possível carregar esta marca.',
        );
        this.carregando.set(false);
      },
    });
  }

  aoEscolherLogo(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.escolherLogo(input.files?.item(0) ?? null);
    input.value = '';
  }

  aoSoltarLogo(event: DragEvent): void {
    event.preventDefault();
    this.escolherLogo(event.dataTransfer?.files.item(0) ?? null);
  }

  permitirSoltar(event: DragEvent): void {
    event.preventDefault();
  }

  removerArquivoLogo(): void {
    this.arquivoLogo.set(null);
    this.trocarPrevia(null);
  }

  private escolherLogo(arquivo: File | null): void {
    if (!arquivo) return;
    const erros = { ...this.errosCampo() };
    delete erros['arquivo'];
    if (!TIPOS_LOGO.includes(arquivo.type)) {
      this.errosCampo.set({ ...erros, arquivo: 'O logo deve ser um arquivo PNG ou SVG.' });
      return;
    }
    if (arquivo.size > TAMANHO_MAXIMO_LOGO) {
      this.errosCampo.set({ ...erros, arquivo: 'O logo deve ter no máximo 2 MB.' });
      return;
    }
    this.errosCampo.set(erros);
    this.arquivoLogo.set(arquivo);
    this.trocarPrevia(URL.createObjectURL(arquivo));
  }

  private trocarPrevia(url: string | null): void {
    const anterior = this.previaArquivo();
    if (anterior) URL.revokeObjectURL(anterior);
    this.previaArquivo.set(url);
  }

  campoInvalido(campo: string): boolean {
    return campo in this.errosCampo();
  }

  mensagemErro(campo: string): string | null {
    return this.errosCampo()[campo] ?? null;
  }

  salvar(publicar = false): void {
    if (this.salvando()) return;
    if (publicar && this.form.controls.idStatus.value === STATUS_PUBLICACAO.RASCUNHO)
      this.form.controls.idStatus.setValue(STATUS_PUBLICACAO.ATIVA);
    this.erroGeral.set(null);
    this.errosCampo.set({});

    const v = this.form.getRawValue();
    const dto: MarcaDTO = {
      nome: v.nome.trim(),
      slug: v.slug.trim(),
      paisOrigem: v.paisOrigem.trim(),
      siteOficial: v.siteOficial.trim(),
      descricao: v.descricao.trim(),
      logoUrl: v.logoUrl.trim(),
      idStatus: Number(v.idStatus),
      destacarHome: v.destacarHome,
    };

    this.salvando.set(true);
    const id = this.marcaId();
    const arquivo = this.arquivoLogo();
    let request$: Observable<unknown>;
    if (!arquivo) {
      request$ = id ? this.marcaService.atualizar(id, dto) : this.marcaService.criar(dto);
    } else {
      // Marca nova com arquivo: nasce como rascunho (o logo ainda não existe), recebe o
      // upload e só então ganha o status escolhido junto com o logoUrl gravado
      const marca$: Observable<number> = id
        ? of(id)
        : this.marcaService
            .criar({ ...dto, idStatus: STATUS_PUBLICACAO.RASCUNHO })
            .pipe(map((m: Marca) => m.id));
      request$ = marca$.pipe(
        switchMap((idMarca) => {
          this.marcaId.set(idMarca);
          return this.marcaService
            .enviarLogo(idMarca, arquivo)
            .pipe(
              switchMap(({ logoUrl }) =>
                this.marcaService.atualizar(idMarca, { ...dto, logoUrl }),
              ),
            );
        }),
      );
    }

    request$.subscribe({
      next: () => {
        this.salvando.set(false);
        this.removerArquivoLogo();
        this.router.navigate(['/admin/marcas']);
      },
      error: (err: HttpErrorResponse) => this.tratarErro(err),
    });
  }

  excluir(): void {
    const id = this.marcaId();
    if (id === null) return;
    if (!confirm('Excluir esta marca? Essa ação não pode ser desfeita.')) return;

    this.excluindo.set(true);
    this.marcaService.excluir(id).subscribe({
      next: () => this.router.navigate(['/admin/marcas']),
      error: () => {
        this.excluindo.set(false);
        this.erroGeral.set(
          'Não foi possível excluir esta marca. Ela pode estar vinculada a produtos.',
        );
      },
    });
  }

  private tratarErro(err: HttpErrorResponse): void {
    this.salvando.set(false);
    const problem = err.error as ProblemDetail | undefined;
    if (problem?.errors?.length) {
      const mapa: Record<string, string> = {};
      for (const fe of problem.errors) mapa[fe.field] = fe.message;
      this.errosCampo.set(mapa);
      this.erroGeral.set(problem.detail ?? 'Verifique os campos destacados.');
    } else {
      this.erroGeral.set(problem?.detail ?? 'Não foi possível salvar a marca. Tente novamente.');
    }
  }
}
