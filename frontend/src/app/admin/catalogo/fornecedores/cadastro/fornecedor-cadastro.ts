import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { AdminNav } from '../../../../layout/admin/admin-nav/admin-nav';
import { FornecedorService } from '../../../../core/services/fornecedor.service';
import { FornecedorDTO, StatusFornecedor } from '../../../../core/models/fornecedor.model';
import { ProblemDetail } from '../../../../core/models/problem.model';

@Component({
  selector: 'app-fornecedor-cadastro',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, AdminNav],
  templateUrl: './fornecedor-cadastro.html',
  styleUrl: './fornecedor-cadastro.scss',
})
export class FornecedorCadastro implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly fornecedorService = inject(FornecedorService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly fornecedorId = signal<number | null>(null);
  readonly modoEdicao = computed(() => this.fornecedorId() !== null);

  readonly carregando = signal(false);
  readonly salvando = signal(false);
  readonly excluindo = signal(false);
  readonly erroGeral = signal<string | null>(null);
  readonly errosCampo = signal<Record<string, string>>({});

  readonly form = this.fb.nonNullable.group({
    razaoSocial: [''],
    cnpj: [''],
    nomeContato: [''],
    emailContato: [''],
    telefoneContato: [''],
    status: ['ATIVO' as StatusFornecedor, Validators.required],
  });

  // Fornecedor nao tem RASCUNHO: os obrigatorios valem para qualquer status.
  readonly pendencias = computed(() => {
    const v = this.form.getRawValue();
    const faltando: string[] = [];
    if (!v.razaoSocial.trim()) faltando.push('Razão social');
    if (!v.cnpj.trim()) faltando.push('CNPJ');
    if (!v.nomeContato.trim()) faltando.push('Nome do contato');
    if (!v.emailContato.trim()) faltando.push('E-mail do contato');
    return faltando;
  });

  ngOnInit(): void {
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
        if (!fornecedor) {
          this.erroGeral.set('Este fornecedor não foi encontrado.');
          this.carregando.set(false);
          return;
        }
        this.form.patchValue({
          razaoSocial: fornecedor.razaoSocial,
          cnpj: fornecedor.cnpj,
          nomeContato: fornecedor.nomeContato,
          emailContato: fornecedor.emailContato,
          telefoneContato: fornecedor.telefoneContato ?? '',
          status: fornecedor.status,
        });
        this.carregando.set(false);
      },
      error: () => {
        this.erroGeral.set('Não foi possível carregar este fornecedor.');
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
    const dto: FornecedorDTO = {
      razaoSocial: v.razaoSocial.trim(),
      cnpj: v.cnpj.trim(),
      nomeContato: v.nomeContato.trim(),
      emailContato: v.emailContato.trim(),
      telefoneContato: v.telefoneContato.trim(),
      status: v.status,
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
        problem?.detail ?? 'Não foi possível salvar o fornecedor. Tente novamente.',
      );
    }
  }
}
