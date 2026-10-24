import { toSignal } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable, map } from 'rxjs';
import { MarcaService } from '../../../../core/services/marca.service';
import { MarcaDTO } from '../../../../core/models/marca.model';
import { STATUS_PUBLICACAO } from '../../../../core/models/status.model';
import { ProblemDetail } from '../../../../core/models/problem.model';

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
    if (!v.logoUrl.trim()) faltando.push('Logo da marca (URL)');
    return faltando;
  });

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
    const request$: Observable<unknown> = id
      ? this.marcaService.atualizar(id, dto)
      : this.marcaService.criar(dto);

    request$.subscribe({
      next: () => {
        this.salvando.set(false);
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
