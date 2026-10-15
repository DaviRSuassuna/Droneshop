import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { AdminNav } from '../../../../layout/admin/admin-nav/admin-nav';
import { CategoriaService } from '../../../../core/services/categoria.service';
import { CategoriaDTO } from '../../../../core/models/categoria.model';
import { STATUS_PUBLICACAO } from '../../../../core/models/status.model';
import { TIPO_PRODUTO } from '../../../../core/models/produto.model';
import { ProblemDetail } from '../../../../core/models/problem.model';

@Component({
  selector: 'app-categoria-cadastro',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, AdminNav],
  templateUrl: './categoria-cadastro.html',
  styleUrl: './categoria-cadastro.scss',
})
export class CategoriaCadastro implements OnInit {
  readonly STATUS = STATUS_PUBLICACAO;
  readonly TIPO = TIPO_PRODUTO;
  private readonly fb = inject(FormBuilder);
  private readonly categoriaService = inject(CategoriaService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly categoriaId = signal<number | null>(null);
  readonly modoEdicao = computed(() => this.categoriaId() !== null);

  readonly carregando = signal(false);
  readonly salvando = signal(false);
  readonly excluindo = signal(false);
  readonly erroGeral = signal<string | null>(null);
  readonly errosCampo = signal<Record<string, string>>({});

  readonly form = this.fb.nonNullable.group({
    nome: [''],
    slug: [''],
    ordemExibicao: [0],
    descricao: [''],
    iconeUrl: [''],
    idStatus: [STATUS_PUBLICACAO.RASCUNHO as number, Validators.required],
    idTipo: this.fb.control<number | null>(null),
    exibirMenu: [false],
  });

  readonly pendencias = computed(() => {
    const v = this.form.getRawValue();
    if (v.idStatus === STATUS_PUBLICACAO.RASCUNHO) return [];
    const faltando: string[] = [];
    if (!v.nome.trim()) faltando.push('Nome da categoria');
    if (v.ordemExibicao === null || Number(v.ordemExibicao) < 0) faltando.push('Ordem de exibição');
    if (!v.idTipo) faltando.push('Tipo de produto');
    return faltando;
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = Number(idParam);
      this.categoriaId.set(id);
      this.carregarCategoria(id);
    }
  }

  private carregarCategoria(id: number): void {
    this.carregando.set(true);
    this.categoriaService.buscarPorId(id).subscribe({
      next: (categoria) => {
        this.form.patchValue({
          nome: categoria.nome,
          slug: categoria.slug,
          ordemExibicao: categoria.ordemExibicao,
          descricao: categoria.descricao ?? '',
          iconeUrl: categoria.iconeUrl ?? '',
          idStatus: categoria.status.id,
          idTipo: categoria.tipo?.id ?? null,
          exibirMenu: categoria.exibirMenu,
        });
        this.carregando.set(false);
      },
      error: (err) => {
        this.erroGeral.set(err.status === 404 ? 'Esta categoria não foi encontrada.' : 'Não foi possível carregar esta categoria.');
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

  salvar(): void {
    this.erroGeral.set(null);
    this.errosCampo.set({});

    const v = this.form.getRawValue();
    const dto: CategoriaDTO = {
      nome: v.nome.trim(),
      slug: v.slug.trim(),
      ordemExibicao: Number(v.ordemExibicao),
      descricao: v.descricao.trim(),
      iconeUrl: v.iconeUrl.trim(),
      idStatus: Number(v.idStatus),
      idTipo: v.idTipo ? Number(v.idTipo) : null,
      exibirMenu: v.exibirMenu,
    };

    this.salvando.set(true);
    const id = this.categoriaId();
    const request$: Observable<unknown> = id
      ? this.categoriaService.atualizar(id, dto)
      : this.categoriaService.criar(dto);

    request$.subscribe({
      next: () => {
        this.salvando.set(false);
        this.router.navigate(['/admin/categorias']);
      },
      error: (err: HttpErrorResponse) => this.tratarErro(err),
    });
  }

  excluir(): void {
    const id = this.categoriaId();
    if (id === null) return;
    if (!confirm('Excluir esta categoria? Essa ação não pode ser desfeita.')) return;

    this.excluindo.set(true);
    this.categoriaService.excluir(id).subscribe({
      next: () => this.router.navigate(['/admin/categorias']),
      error: () => {
        this.excluindo.set(false);
        this.erroGeral.set(
          'Não foi possível excluir esta categoria. Ela pode ter subcategorias vinculadas.',
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
      this.erroGeral.set(
        problem?.detail ?? 'Não foi possível salvar a categoria. Tente novamente.',
      );
    }
  }
}
