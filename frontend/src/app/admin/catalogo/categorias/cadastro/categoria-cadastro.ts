import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { AdminNav } from '../../../../layout/admin/admin-nav/admin-nav';
import { CategoriaService } from '../../../../core/services/categoria.service';
import { Categoria, CategoriaDTO, StatusCategoria } from '../../../../core/models/categoria.model';
import { ProblemDetail } from '../../../../core/models/problem.model';

@Component({
  selector: 'app-categoria-cadastro', standalone: true,
  imports: [ReactiveFormsModule, RouterLink, AdminNav],
  templateUrl: './categoria-cadastro.html', styleUrl: './categoria-cadastro.scss',
})
export class CategoriaCadastro implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(CategoriaService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  readonly categoriaId = signal<number | null>(null);
  readonly modoEdicao = computed(() => this.categoriaId() !== null);
  readonly carregando = signal(false); readonly salvando = signal(false); readonly excluindo = signal(false);
  readonly erroGeral = signal<string | null>(null); readonly errosCampo = signal<Record<string, string>>({});
  readonly categorias = signal<Categoria[]>([]);
  readonly form = this.fb.nonNullable.group({
    nome: [''], slug: [''], categoriaPaiId: [null as number | null], ordemExibicao: [0], descricao: [''],
    status: ['RASCUNHO' as StatusCategoria], exibirMenu: [false], iconeUrl: [''],
  });
  readonly pendencias = computed(() => this.form.controls.status.value === 'RASCUNHO' || this.form.controls.nome.value.trim() ? [] : ['Nome da categoria']);

  ngOnInit(): void {
    this.service.listar().subscribe({ next: (categorias) => this.categorias.set(categorias) });
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) { const id = Number(idParam); this.categoriaId.set(id); this.carregar(id); }
  }
  private carregar(id: number): void {
    this.carregando.set(true);
    this.service.buscarPorId(id).subscribe({
      next: (categoria) => { this.form.patchValue({ ...categoria, categoriaPaiId: categoria.categoriaPaiId, descricao: categoria.descricao ?? '', iconeUrl: categoria.iconeUrl ?? '' }); this.carregando.set(false); },
      error: () => { this.erroGeral.set('Não foi possível carregar esta categoria.'); this.carregando.set(false); },
    });
  }
  campoInvalido(campo: string): boolean { return campo in this.errosCampo(); }
  mensagemErro(campo: string): string | null { return this.errosCampo()[campo] ?? null; }
  salvar(): void {
    this.erroGeral.set(null); this.errosCampo.set({});
    const v = this.form.getRawValue();
    const dto: CategoriaDTO = { nome: v.nome.trim(), slug: v.slug.trim(), categoriaPaiId: v.categoriaPaiId, ordemExibicao: Number(v.ordemExibicao), descricao: v.descricao.trim(), status: v.status, exibirMenu: v.exibirMenu, iconeUrl: v.iconeUrl.trim() };
    this.salvando.set(true);
    const id = this.categoriaId();
    const request$: Observable<unknown> = id ? this.service.atualizar(id, dto) : this.service.criar(dto);
    request$.subscribe({ next: () => { this.salvando.set(false); this.router.navigate(['/admin/categorias']); }, error: (err: HttpErrorResponse) => this.tratarErro(err) });
  }
  excluir(): void {
    const id = this.categoriaId(); if (id === null || !confirm('Excluir esta categoria? Essa ação não pode ser desfeita.')) return;
    this.excluindo.set(true); this.service.excluir(id).subscribe({ next: () => this.router.navigate(['/admin/categorias']), error: () => { this.excluindo.set(false); this.erroGeral.set('Não foi possível excluir esta categoria.'); } });
  }
  private tratarErro(err: HttpErrorResponse): void {
    this.salvando.set(false); const problem = err.error as ProblemDetail | undefined;
    if (problem?.errors?.length) { const mapa: Record<string, string> = {}; for (const erro of problem.errors) mapa[erro.field] = erro.message; this.errosCampo.set(mapa); }
    this.erroGeral.set(problem?.detail ?? 'Não foi possível salvar a categoria. Tente novamente.');
  }
}
