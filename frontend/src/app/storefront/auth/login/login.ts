import { Component, OnInit, inject, signal } from '@angular/core';
import { NgOptimizedImage } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ContaService } from '../../../core/services/conta.service';
import { AuthService } from '../../../core/services/auth.service';
import { LoginResponse } from '../../../core/models/auth.model';
import { TIPO_PESSOA } from '../../../core/models/cliente.model';
import { ProblemDetail } from '../../../core/models/problem.model';
import { somenteDigitos } from '../../../core/utils/documentos';

@Component({
  selector: 'app-login',
  imports: [FormsModule, NgOptimizedImage],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly conta = inject(ContaService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly aba = signal<'entrar' | 'criar'>('entrar');

  email = '';
  senha = '';
  readonly carregando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly otpNecessario = signal(false);
  readonly aviso = signal<string | null>(null);
  codigoOtp = '';

  novo = { nome: '', email: '', documento: '', senha: '', confirmar: '' };
  readonly erroCriar = signal<string | null>(null);
  readonly errosCampo = signal<Record<string, string>>({});

  entrar(): void {
    if (this.carregando()) return;
    this.erro.set(null);
    this.aviso.set(null);
    const codigoOtp = this.otpNecessario() ? this.codigoOtp.trim() : undefined;
    if (codigoOtp !== undefined && !/^\d{6}$/.test(codigoOtp)) {
      this.erro.set('Digite os 6 dígitos do seu app autenticador.');
      return;
    }

    this.carregando.set(true);
    this.auth.login(this.email.trim(), this.senha, codigoOtp).subscribe({
      next: (resposta) => {
        this.carregando.set(false);
        this.senha = '';
        this.limparOtp();
        this.irParaDestino(resposta);
      },
      error: (resposta: HttpErrorResponse) => {
        this.carregando.set(false);
        const problema = resposta.error as ProblemDetail | null;
        if (resposta.status === 401 && problema?.codigo === 'otp-necessario') {
          this.otpNecessario.set(true);
          return;
        }
        this.erro.set(
          resposta.status === 401
            ? codigoOtp
              ? 'E-mail, senha ou código inválidos.'
              : 'E-mail ou senha inválidos.'
            : (problema?.detail ?? 'Não foi possível entrar. Tente novamente.'),
        );
      },
    });
  }

  limparOtp(): void {
    this.otpNecessario.set(false);
    this.codigoOtp = '';
  }

  recuperarSenha(campo: HTMLInputElement): void {
    if (this.carregando() || !campo.reportValidity()) return;
    this.carregando.set(true);
    this.aviso.set(null);
    this.erro.set(null);
    this.auth.recuperarSenha(this.email.trim()).subscribe({
      next: () => {
        this.carregando.set(false);
        this.aviso.set(
          'Se houver uma conta ativa com este e-mail, você receberá um link para redefinir a senha.',
        );
      },
      error: (resposta: HttpErrorResponse) => {
        this.carregando.set(false);
        const problema = resposta.error as ProblemDetail | null;
        this.erro.set(problema?.detail ?? 'Não foi possível solicitar a recuperação de senha.');
      },
    });
  }

  /** Termos e política vigentes; criar a conta é aceitar os dois (#46). */
  private readonly termos = signal<{ tipo: string; versao: string }[]>([]);

  ngOnInit(): void {
    this.conta.termos().subscribe({
      next: (t) =>
        this.termos.set(
          (Array.isArray(t) ? t : [])
            .filter((x) => x.obrigatorio)
            .map(({ tipo, versao }) => ({ tipo, versao })),
        ),
      error: () => this.termos.set([]),
    });
  }

  erroCampo(campo: string): string | null {
    return this.errosCampo()[campo] ?? null;
  }

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
    this.auth
      .cadastro({
        nome: this.novo.nome.trim(),
        email,
        senha: this.novo.senha,
        idTipoPessoa: documento.length === 14 ? TIPO_PESSOA.PJ : TIPO_PESSOA.PF,
        documento,
        telefone: '',
        consentimentos: this.termos(),
      })
      .subscribe({
        next: () => {
          this.carregando.set(false);
          this.novo = { nome: '', email: '', documento: '', senha: '', confirmar: '' };
          this.email = email;
          this.senha = '';
          this.limparOtp();
          this.erro.set(null);
          this.aviso.set(
            'Conta criada. Você já pode entrar; enviamos um e-mail para confirmar o endereço.',
          );
          this.aba.set('entrar');
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

  private irParaDestino(resposta: LoginResponse): void {
    const volta = this.route.snapshot.queryParamMap.get('volta');
    const admin = resposta.papeis.includes('ADMINISTRATIVO') || resposta.papeis.includes('ADMIN');
    const padrao = admin ? '/admin/marcas' : '/conta/perfil';
    const destino = volta && /^\/(?![\/\\])/.test(volta) ? volta : padrao;
    void this.router.navigateByUrl(destino);
  }
}
