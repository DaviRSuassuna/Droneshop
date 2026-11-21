import { Component, inject, signal } from '@angular/core';
import { NgOptimizedImage } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { switchMap } from 'rxjs';
import { AuthService, LoginResponse, PERFIL_ATIVO } from '../../../core/services/auth.service';
import { ClienteService } from '../../../core/services/cliente.service';
import { TIPO_PESSOA } from '../../../core/models/cliente.model';
import { ProblemDetail } from '../../../core/models/problem.model';
import { somenteDigitos } from '../../../core/utils/documentos';

@Component({
  selector: 'app-login',
  imports: [FormsModule, NgOptimizedImage],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly clientes = inject(ClienteService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly aba = signal<'entrar' | 'criar'>('entrar');

  email = '';
  senha = '';
  manterConectado = false;
  readonly carregando = signal(false);
  readonly erro = signal<string | null>(null);
  /** Quem tem os dois perfis escolhe com qual entrar (#20). */
  readonly escolhendoPerfil = signal(false);
  protected readonly PERFIL_ATIVO = PERFIL_ATIVO;

  novo = { nome: '', email: '', documento: '', senha: '', confirmar: '' };
  readonly erroCriar = signal<string | null>(null);
  readonly errosCampo = signal<Record<string, string>>({});

  entrar(): void {
    if (this.carregando()) return;
    this.carregando.set(true);
    this.erro.set(null);
    this.auth.login(this.email.trim(), this.senha, this.manterConectado).subscribe({
      next: (resposta) => {
        this.carregando.set(false);
        this.irParaDestino(resposta);
      },
      error: (resposta: { status?: number; error?: { detail?: string } }) => {
        this.carregando.set(false);
        this.erro.set(
          resposta.status === 401
            ? 'E-mail ou senha inválidos.'
            : (resposta.error?.detail ?? 'Não foi possível entrar. Tente novamente.'),
        );
      },
    });
  }

  escolher(idPerfilAtivo: number): void {
    if (this.carregando()) return;
    this.carregando.set(true);
    this.auth.escolherPerfil(idPerfilAtivo).subscribe({
      next: (resposta) => {
        this.carregando.set(false);
        this.escolhendoPerfil.set(false);
        this.irParaDestino(resposta);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível entrar com esse perfil. Tente novamente.');
      },
    });
  }

  erroCampo(campo: string): string | null {
    return this.errosCampo()[campo] ?? null;
  }

  /** Cria a conta (o tipo de pessoa sai do tamanho do documento) e já entra. */
  criarConta(): void {
    if (this.carregando()) return;
    this.erroCriar.set(null);
    const documento = somenteDigitos(this.novo.documento);
    const erros: Record<string, string> = {};
    if (!this.novo.nome.trim()) erros['nome'] = 'Informe o nome.';
    if (!this.novo.email.trim()) erros['email'] = 'Informe o e-mail.';
    if (documento.length !== 11 && documento.length !== 14)
      erros['documento'] = 'Informe um CPF (11 dígitos) ou CNPJ (14 dígitos).';
    if (this.novo.senha.length < 8) erros['senha'] = 'A senha precisa de pelo menos 8 caracteres.';
    else if (this.novo.senha !== this.novo.confirmar)
      erros['confirmar'] = 'As senhas não conferem.';
    this.errosCampo.set(erros);
    if (Object.keys(erros).length) return;

    this.carregando.set(true);
    const email = this.novo.email.trim();
    this.clientes
      .criarConta({
        nome: this.novo.nome.trim(),
        email,
        senha: this.novo.senha,
        idTipoPessoa: documento.length === 14 ? TIPO_PESSOA.PJ : TIPO_PESSOA.PF,
        documento,
        telefone: '',
      })
      .pipe(switchMap(() => this.auth.login(email, this.novo.senha, false)))
      .subscribe({
        next: (resposta) => {
          this.carregando.set(false);
          this.irParaDestino(resposta);
        },
        error: (resposta: HttpErrorResponse) => {
          this.carregando.set(false);
          const problema = resposta.error as ProblemDetail | null;
          const porCampo: Record<string, string> = {};
          for (const e of problema?.errors ?? []) porCampo[e.field] = e.message;
          this.errosCampo.set(porCampo);
          this.erroCriar.set(
            Object.keys(porCampo).length
              ? null
              : (problema?.detail ?? 'Não foi possível criar a conta. Tente novamente.'),
          );
        },
      });
  }

  /** Volta para onde estava; sem destino, cliente vai para a conta e admin para o painel. */
  private irParaDestino(resposta: LoginResponse): void {
    if (resposta.escolherPerfil) {
      this.escolhendoPerfil.set(true);
      return;
    }
    const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
    const padrao = (resposta.perfilAtivo ?? resposta.perfis[0]) === 'ADMINISTRATIVO' ? '/admin/marcas' : '/conta/perfil';
    const destino = returnUrl?.startsWith('/') && !returnUrl.startsWith('//') ? returnUrl : padrao;
    void this.router.navigateByUrl(destino);
  }
}
