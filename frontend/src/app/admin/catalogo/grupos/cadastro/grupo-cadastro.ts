import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable, startWith } from 'rxjs';
import { GrupoProdutoService } from '../../../../core/services/grupo-produto.service';
import { GrupoProduto, VersaoGrupo } from '../../../../core/models/grupo-produto.model';
import { STATUS_PRODUTO } from '../../../../core/models/status.model';
import { TIPO_PRODUTO } from '../../../../core/models/produto.model';
import { ProblemDetail } from '../../../../core/models/problem.model';

/**
 * Cadastro de grupo de produto (D40). O grupo guarda nome, slug, descrição e status; as
 * versões (rótulo, ordem, entrar e sair do grupo) se gravam no cadastro de cada produto, então
 * a tabela de versões aqui só mostra e leva até o produto.
 */
@Component({
  selector: 'app-grupo-cadastro',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './grupo-cadastro.html',
})
export class GrupoCadastro implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly grupoService = inject(GrupoProdutoService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly grupoId = signal<number | null>(null);
  readonly modoEdicao = computed(() => this.grupoId() !== null);
  readonly grupo = signal<GrupoProduto | null>(null);

  readonly carregando = signal(false);
  readonly salvando = signal(false);
  readonly excluindo = signal(false);
  readonly erroGeral = signal<string | null>(null);
  readonly errosCampo = signal<Record<string, string>>({});

  readonly form = this.fb.nonNullable.group({
    nome: [''],
    slug: [''],
    descricaoCurta: [''],
    ativo: [true],
  });
  private readonly valores = toSignal(this.form.valueChanges.pipe(startWith(this.form.getRawValue())), {
    requireSync: true,
  });

  private readonly formatoMoeda = new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
    maximumFractionDigits: 0,
  });

  readonly versoes = computed(() => this.grupo()?.versoes ?? []);
  readonly versoesAtivas = computed(() => this.versoes().filter((v) => v.status.id === STATUS_PRODUTO.ATIVO));
  readonly nomePrevia = computed(() => this.valores().nome?.trim() || 'Nome do grupo');

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) return;
    const id = Number(idParam);
    this.grupoId.set(id);
    this.carregando.set(true);
    this.grupoService.buscarPorId(id).subscribe({
      next: (grupo) => {
        this.grupo.set(grupo);
        this.form.setValue({
          nome: grupo.nome,
          slug: grupo.slug,
          descricaoCurta: grupo.descricaoCurta ?? '',
          ativo: grupo.ativo,
        });
        this.carregando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.erroGeral.set(err.status === 404 ? 'Este grupo não foi encontrado.' : 'Não foi possível carregar o grupo.');
        this.carregando.set(false);
      },
    });
  }

  mensagemErro(campo: string): string | null {
    return this.errosCampo()[campo] ?? null;
  }

  rotaProduto(v: VersaoGrupo): (string | number)[] {
    const tipo = v.tipo.id === TIPO_PRODUTO.DRONE ? 'drones' : v.tipo.id === TIPO_PRODUTO.KIT ? 'kits' : 'acessorios';
    return ['/admin', tipo, v.idProduto, 'editar'];
  }

  nomeTipo(v: VersaoGrupo): string {
    return v.tipo.id === TIPO_PRODUTO.DRONE ? 'Drone' : v.tipo.id === TIPO_PRODUTO.KIT ? 'Kit' : 'Acessório';
  }

  classeStatus(v: VersaoGrupo): string {
    if (v.status.id === STATUS_PRODUTO.ATIVO) return 'st ok';
    return v.status.id === STATUS_PRODUTO.INATIVO ? 'st warn' : 'st mute';
  }

  preco(valor: number | null): string {
    return valor === null ? '—' : this.formatoMoeda.format(valor);
  }

  salvar(): void {
    this.erroGeral.set(null);
    this.errosCampo.set({});
    const v = this.form.getRawValue();
    const dto = { nome: v.nome.trim(), slug: v.slug.trim(), descricaoCurta: v.descricaoCurta.trim(), ativo: v.ativo };
    const id = this.grupoId();
    this.salvando.set(true);
    const request$: Observable<unknown> = id ? this.grupoService.atualizar(id, dto) : this.grupoService.criar(dto);
    request$.subscribe({
      next: () => this.router.navigate(['/admin/grupos']),
      error: (err: HttpErrorResponse) => {
        this.salvando.set(false);
        const problem = err.error as ProblemDetail | undefined;
        if (problem?.errors?.length) {
          const mapa: Record<string, string> = {};
          for (const fe of problem.errors) mapa[fe.field] = fe.message;
          this.errosCampo.set(mapa);
        }
        this.erroGeral.set(problem?.detail ?? 'Não foi possível salvar o grupo. Tente novamente.');
      },
    });
  }

  excluir(): void {
    const id = this.grupoId();
    if (id === null || !confirm('Excluir este grupo? Essa ação não pode ser desfeita.')) return;
    this.excluindo.set(true);
    this.grupoService.excluir(id).subscribe({
      next: () => this.router.navigate(['/admin/grupos']),
      error: (err: HttpErrorResponse) => {
        this.excluindo.set(false);
        // 409: grupo com produtos (D24); o detail orienta tirar as versões ou inativar
        this.erroGeral.set((err.error as ProblemDetail | undefined)?.detail ?? 'Não foi possível excluir o grupo.');
      },
    });
  }
}
