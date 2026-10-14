import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { AdminNav } from '../../../../layout/admin/admin-nav/admin-nav';
import { SubcategoriaService } from '../../../../core/services/subcategoria.service';
import { CategoriaService } from '../../../../core/services/categoria.service';
import { SubcategoriaDTO } from '../../../../core/models/subcategoria.model';
import { Categoria } from '../../../../core/models/categoria.model';
import { STATUS_PUBLICACAO } from '../../../../core/models/status.model';
import { ProblemDetail } from '../../../../core/models/problem.model';

@Component({
  selector: 'app-subcategoria-cadastro',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, AdminNav],
  templateUrl: './subcategoria-cadastro.html',
})
export class SubcategoriaCadastro implements OnInit {
  readonly STATUS = STATUS_PUBLICACAO;
  private readonly fb = inject(FormBuilder);
  private readonly subcategoriaService = inject(SubcategoriaService);
  private readonly categoriaService = inject(CategoriaService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly subcategoriaId = signal<number | null>(null);
  readonly modoEdicao = computed(() => this.subcategoriaId() !== null);

  readonly categorias = signal<Categoria[]>([]);

  readonly carregando = signal(false);
  readonly salvando = signal(false);
  readonly excluindo = signal(false);
  readonly erroGeral = signal<string | null>(null);
  readonly errosCampo = signal<Record<string, string>>({});

  readonly form = this.fb.group({
    nome: this.fb.nonNullable.control(''),
    slug: this.fb.nonNullable.control(''),
    idCategoria: this.fb.control<number | null>(null),
    ordemExibicao: this.fb.nonNullable.control(0),
    descricao: this.fb.nonNullable.control(''),
    idStatus: this.fb.nonNullable.control<number>(STATUS_PUBLICACAO.ATIVA, Validators.required),
  });

  ngOnInit(): void {
    this.categoriaService
      .listar(0, 1000)
      .subscribe((p) => this.categorias.set(p.items.filter((x) => x.nome)));

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = Number(idParam);
      this.subcategoriaId.set(id);
      this.carregar(id);
    }
  }

  private carregar(id: number): void {
    this.carregando.set(true);
    this.subcategoriaService.buscarPorId(id).subscribe({
      next: (sub) => {
        this.form.patchValue({
          nome: sub.nome,
          slug: sub.slug,
          idCategoria: sub.categoria.id,
          ordemExibicao: sub.ordemExibicao,
          descricao: sub.descricao ?? '',
          idStatus: sub.status.id,
        });
        this.carregando.set(false);
      },
      error: (err) => {
        this.erroGeral.set(err.status === 404 ? 'Esta subcategoria não foi encontrada.' : 'Não foi possível carregar esta subcategoria.');
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
    const dto: SubcategoriaDTO = {
      nome: v.nome.trim(),
      slug: v.slug.trim(),
      idCategoria: v.idCategoria ? Number(v.idCategoria) : null,
      ordemExibicao: Number(v.ordemExibicao),
      descricao: v.descricao.trim(),
      idStatus: Number(v.idStatus),
    };

    this.salvando.set(true);
    const id = this.subcategoriaId();
    const request$: Observable<unknown> = id
      ? this.subcategoriaService.atualizar(id, dto)
      : this.subcategoriaService.criar(dto);

    request$.subscribe({
      next: () => {
        this.salvando.set(false);
        this.router.navigate(['/admin/subcategorias']);
      },
      error: (err: HttpErrorResponse) => this.tratarErro(err),
    });
  }

  excluir(): void {
    const id = this.subcategoriaId();
    if (id === null) return;
    if (!confirm('Excluir esta subcategoria? Essa ação não pode ser desfeita.')) return;

    this.excluindo.set(true);
    this.subcategoriaService.excluir(id).subscribe({
      next: () => this.router.navigate(['/admin/subcategorias']),
      error: () => {
        this.excluindo.set(false);
        this.erroGeral.set(
          'Não foi possível excluir esta subcategoria. Ela pode ter produtos vinculados.',
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
        problem?.detail ?? 'Não foi possível salvar a subcategoria. Tente novamente.',
      );
    }
  }
}
