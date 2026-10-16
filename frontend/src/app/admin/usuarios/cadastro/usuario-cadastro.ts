import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable, forkJoin } from 'rxjs';
import { AdminNav } from '../../../layout/admin/admin-nav/admin-nav';
import {
  ModuloSistema,
  PapelAdministrativo,
  PermissaoModulo,
  UsuarioAdministrativoDTO,
} from '../../../core/models/usuario.model';
import { ProblemDetail } from '../../../core/models/problem.model';
import { UsuarioService } from '../../../core/services/usuario.service';

@Component({
  selector: 'app-usuario-cadastro',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, AdminNav],
  templateUrl: './usuario-cadastro.html',
  styleUrl: './usuario-cadastro.scss',
})
export class UsuarioCadastro implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly usuarioService = inject(UsuarioService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly usuarioId = signal<number | null>(null);
  readonly modoEdicao = computed(() => this.usuarioId() !== null);
  readonly carregando = signal(true);
  readonly salvando = signal(false);
  readonly erroGeral = signal<string | null>(null);
  readonly errosCampo = signal<Record<string, string>>({});
  readonly papeis = signal<PapelAdministrativo[]>([]);
  readonly modulos = signal<ModuloSistema[]>([]);
  readonly permissoes = signal<PermissaoModulo[]>([]);
  private readonly permissoesAlteradas = signal(new Set<number>());

  readonly form = this.fb.nonNullable.group({
    nome: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    senha: [''],
    idPapel: [0, Validators.min(1)],
    ativo: [true],
  });

  readonly camposObrigatoriosPendentes = computed(() => {
    const valor = this.form.getRawValue();
    const pendencias: string[] = [];
    if (!valor.nome.trim()) pendencias.push('Nome completo');
    if (!valor.email.trim() || this.form.controls.email.invalid) pendencias.push('E-mail corporativo');
    if (!this.modoEdicao() && valor.senha.length < 8) pendencias.push('Senha provisória');
    if (!valor.idPapel) pendencias.push('Papel / permissão');
    return pendencias;
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) this.usuarioId.set(Number(id));

    forkJoin({
      papeis: this.usuarioService.papeis(),
      modulos: this.usuarioService.modulos(),
    }).subscribe({
      next: ({ papeis, modulos }) => {
        this.papeis.set(papeis);
        this.modulos.set(modulos);
        if (this.usuarioId() === null) {
          this.carregando.set(false);
          return;
        }
        const id = this.usuarioId();
        if (id !== null) this.carregarUsuario(id);
      },
      error: () => {
        this.erroGeral.set('Não foi possível carregar papéis e módulos. Tente novamente.');
        this.carregando.set(false);
      },
    });
  }

  private carregarUsuario(id: number): void {
    this.usuarioService.buscarPorId(id).subscribe({
      next: (usuario) => {
        this.form.patchValue({
          nome: usuario.nome,
          email: usuario.email,
          idPapel: usuario.papel.id,
          ativo: usuario.ativo,
        });
        this.usuarioService.permissoesPadrao(usuario.papel.id).subscribe({
          next: (padroes) => {
            this.permissoes.set(this.sobreporPermissoes(padroes, usuario.permissoes));
            this.permissoesAlteradas.set(new Set(this.modulos().map((m) => m.id)));
            this.carregando.set(false);
          },
          error: () => {
            this.erroGeral.set('Não foi possível carregar as permissões do papel.');
            this.carregando.set(false);
          },
        });
      },
      error: (err: HttpErrorResponse) => {
        this.erroGeral.set(
          err.status === 404 ? 'Este usuário não foi encontrado.' : 'Não foi possível carregar o usuário.',
        );
        this.carregando.set(false);
      },
    });
  }

  aoMudarPapel(idPapel: string): void {
    const id = Number(idPapel);
    this.form.controls.idPapel.setValue(id);
    this.permissoesAlteradas.set(new Set());
    this.permissoes.set([]);
    if (!id) return;

    this.usuarioService.permissoesPadrao(id).subscribe({
      next: (padroes) => this.permissoes.set(padroes),
      error: () => this.erroGeral.set('Não foi possível carregar as permissões padrão deste papel.'),
    });
  }

  nomeModulo(modulo: ModuloSistema): string {
    switch (modulo.nome) {
      case 'CATALOGO':
        return 'Produtos, marcas e categorias';
      case 'ESTOQUE':
        return 'Estoque';
      case 'PEDIDO':
        return 'Pedidos';
      case 'PAGAMENTO':
        return 'Pagamentos';
      case 'DEVOLUCAO':
        return 'Devoluções';
      case 'ATENDIMENTO':
        return 'Atendimento';
      case 'NOTIFICACAO':
        return 'Notificações';
      case 'ADMINISTRACAO':
        return 'Usuários e configuração';
      default:
        return modulo.nome;
    }
  }

  nomePapel(nome: string): string {
    switch (nome) {
      case 'ADMIN':
        return 'Administrador';
      case 'CATALOGO':
        return 'Catálogo';
      case 'OPERADOR':
        return 'Operações';
      case 'ATENDIMENTO':
        return 'Atendimento';
      case 'SOMENTE_LEITURA':
        return 'Somente leitura';
      default:
        return nome;
    }
  }

  atualizarPermissao(
    idModulo: number,
    campo: 'podeVer' | 'podeEditar' | 'podeExcluir',
    valor: boolean,
  ): void {
    this.permissoes.update((lista) =>
      lista.map((permissao) =>
        permissao.modulo.id === idModulo ? { ...permissao, [campo]: valor } : permissao,
      ),
    );
    this.permissoesAlteradas.update((anteriores) => new Set(anteriores).add(idModulo));
  }

  salvar(): void {
    this.erroGeral.set(null);
    this.errosCampo.set({});
    this.form.markAllAsTouched();

    const valor = this.form.getRawValue();
    if (this.form.controls.nome.invalid || !valor.nome.trim()) {
      this.errosCampo.set({ nome: 'O nome é obrigatório.' });
    }
    if (this.form.controls.email.invalid || !valor.email.trim()) {
      this.errosCampo.update((erros) => ({ ...erros, email: 'Informe um e-mail válido.' }));
    }
    if (!this.modoEdicao() && valor.senha.length < 8) {
      this.errosCampo.update((erros) => ({ ...erros, senha: 'A senha deve ter no mínimo 8 caracteres.' }));
    }
    if (!valor.idPapel) {
      this.errosCampo.update((erros) => ({ ...erros, idPapel: 'Selecione um papel administrativo.' }));
    }
    if (Object.keys(this.errosCampo()).length > 0) return;

    const idsAlterados = this.permissoesAlteradas();
    const dto: UsuarioAdministrativoDTO = {
      nome: valor.nome.trim(),
      email: valor.email.trim(),
      senha: valor.senha || undefined,
      idPapel: valor.idPapel,
      ativo: valor.ativo,
      permissoes: this.permissoes()
        .filter((p) => idsAlterados.has(p.modulo.id))
        .map((p) => ({
          idModulo: p.modulo.id,
          podeVer: p.podeVer,
          podeEditar: p.podeEditar,
          podeExcluir: p.podeExcluir,
        })),
    };

    this.salvando.set(true);
    const id = this.usuarioId();
    const requisicao: Observable<unknown> = id
      ? this.usuarioService.atualizar(id, dto)
      : this.usuarioService.criar(dto);
    requisicao.subscribe({
      next: () => this.router.navigate(['/admin/usuarios']),
      error: (err: HttpErrorResponse) => this.tratarErro(err),
    });
  }

  private sobreporPermissoes(
    padroes: PermissaoModulo[],
    atuais: PermissaoModulo[],
  ): PermissaoModulo[] {
    const porId = new Map(atuais.map((p) => [p.modulo.id, p]));
    return padroes.map((padrao) => {
      const atual = porId.get(padrao.modulo.id);
      return atual ? { ...padrao, ...atual } : padrao;
    });
  }

  private tratarErro(err: HttpErrorResponse): void {
    this.salvando.set(false);
    const problem = err.error as ProblemDetail | undefined;
    if (problem?.errors?.length) {
      this.errosCampo.set(Object.fromEntries(problem.errors.map((erro) => [erro.field, erro.message])));
    }
    this.erroGeral.set(problem?.detail ?? 'Não foi possível salvar o usuário. Tente novamente.');
  }
}
