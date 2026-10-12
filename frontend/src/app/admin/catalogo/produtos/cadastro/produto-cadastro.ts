import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { AdminNav } from '../../../../layout/admin/admin-nav/admin-nav';
import { ProdutoService } from '../../../../core/services/produto.service';
import { MarcaService } from '../../../../core/services/marca.service';
import { CategoriaService } from '../../../../core/services/categoria.service';
import { SubcategoriaService } from '../../../../core/services/subcategoria.service';
import { FornecedorService } from '../../../../core/services/fornecedor.service';
import { ProdutoDTO, StatusProduto } from '../../../../core/models/produto.model';
import { Marca } from '../../../../core/models/marca.model';
import { Categoria } from '../../../../core/models/categoria.model';
import { Subcategoria } from '../../../../core/models/subcategoria.model';
import { Fornecedor } from '../../../../core/models/fornecedor.model';
import { ProblemDetail } from '../../../../core/models/problem.model';

@Component({
  selector: 'app-produto-cadastro',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, AdminNav],
  templateUrl: './produto-cadastro.html',
})
export class ProdutoCadastro implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly produtoService = inject(ProdutoService);
  private readonly marcaService = inject(MarcaService);
  private readonly categoriaService = inject(CategoriaService);
  private readonly subcategoriaService = inject(SubcategoriaService);
  private readonly fornecedorService = inject(FornecedorService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly produtoId = signal<number | null>(null);
  readonly modoEdicao = computed(() => this.produtoId() !== null);

  readonly marcas = signal<Marca[]>([]);
  readonly categorias = signal<Categoria[]>([]);
  readonly subcategorias = signal<Subcategoria[]>([]);
  readonly fornecedores = signal<Fornecedor[]>([]);
  readonly categoriaSelecionada = signal<number | null>(null);

  /** Subcategorias da categoria escolhida no select de cima. */
  readonly subcategoriasDisponiveis = computed(() =>
    this.subcategorias().filter((s) => s.categoriaId === this.categoriaSelecionada()),
  );

  readonly carregando = signal(false);
  readonly salvando = signal(false);
  readonly excluindo = signal(false);
  readonly erroGeral = signal<string | null>(null);
  readonly errosCampo = signal<Record<string, string>>({});

  readonly form = this.fb.group({
    nome: this.fb.nonNullable.control(''),
    modelo: this.fb.nonNullable.control(''),
    descricaoCurta: this.fb.nonNullable.control(''),
    preco: this.fb.control<number | null>(null),
    marcaId: this.fb.control<number | null>(null),
    // categoriaId só ajuda a escolher; o que vai para a API é subcategoriaId
    categoriaId: this.fb.control<number | null>(null),
    subcategoriaId: this.fb.control<number | null>(null),
    fornecedorId: this.fb.control<number | null>(null),
    status: this.fb.nonNullable.control<StatusProduto>('RASCUNHO', Validators.required),
    destaque: this.fb.nonNullable.control(false),
  });

  ngOnInit(): void {
    this.marcaService.listar(0, 500).subscribe((m) => this.marcas.set(m.filter((x) => x.nome)));
    this.categoriaService
      .listar(0, 500)
      .subscribe((c) => this.categorias.set(c.filter((x) => x.nome)));
    this.subcategoriaService.listar().subscribe((s) => this.subcategorias.set(s));
    this.fornecedorService.listar(0, 500).subscribe((f) => this.fornecedores.set(f));

    // ao trocar a categoria, limpa a subcategoria se ela for de outra categoria
    this.form.controls.categoriaId.valueChanges.subscribe((id) => {
      const categoriaId = id ? Number(id) : null;
      this.categoriaSelecionada.set(categoriaId);
      const sub = this.subcategorias().find(
        (s) => s.id === this.form.controls.subcategoriaId.value,
      );
      if (sub && sub.categoriaId !== categoriaId) this.form.controls.subcategoriaId.setValue(null);
    });

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = Number(idParam);
      this.produtoId.set(id);
      this.carregarProduto(id);
    }
  }

  private carregarProduto(id: number): void {
    this.carregando.set(true);
    this.produtoService.buscarPorId(id).subscribe({
      next: (produto) => {
        this.form.patchValue({
          nome: produto.nome,
          modelo: produto.modelo,
          descricaoCurta: produto.descricaoCurta ?? '',
          preco: produto.preco,
          marcaId: produto.marcaId,
          categoriaId: produto.categoriaId,
          subcategoriaId: produto.subcategoriaId,
          fornecedorId: produto.fornecedorId,
          status: produto.status,
          destaque: produto.destaque,
        });
        this.carregando.set(false);
      },
      error: (err) => {
        this.erroGeral.set(err.status === 404 ? 'Este produto não foi encontrado.' : 'Não foi possível carregar este produto.');
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
    const dto: ProdutoDTO = {
      nome: v.nome.trim(),
      modelo: v.modelo.trim(),
      descricaoCurta: v.descricaoCurta.trim(),
      preco: v.preco,
      // o select devolve string; a API espera Long
      marcaId: v.marcaId ? Number(v.marcaId) : null,
      subcategoriaId: v.subcategoriaId ? Number(v.subcategoriaId) : null,
      fornecedorId: v.fornecedorId ? Number(v.fornecedorId) : null,
      status: v.status,
      destaque: v.destaque,
    };

    this.salvando.set(true);
    const id = this.produtoId();
    const request$: Observable<unknown> = id
      ? this.produtoService.atualizar(id, dto)
      : this.produtoService.criar(dto);

    request$.subscribe({
      next: () => {
        this.salvando.set(false);
        this.router.navigate(['/admin/produtos']);
      },
      error: (err: HttpErrorResponse) => this.tratarErro(err),
    });
  }

  excluir(): void {
    const id = this.produtoId();
    if (id === null) return;
    if (!confirm('Excluir este produto? Essa ação não pode ser desfeita.')) return;

    this.excluindo.set(true);
    this.produtoService.excluir(id).subscribe({
      next: () => this.router.navigate(['/admin/produtos']),
      error: () => {
        this.excluindo.set(false);
        this.erroGeral.set('Não foi possível excluir este produto.');
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
      this.erroGeral.set(problem?.detail ?? 'Não foi possível salvar o produto. Tente novamente.');
    }
  }
}
