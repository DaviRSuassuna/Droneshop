import { DatePipe } from '@angular/common';
import { AuthService, SessaoUsuario } from '../../../core/services/auth.service';
import { AccountMenu } from '../../../layout/storefront/account-menu/account-menu';
import { ContaSessao } from '../conta-sessao';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, OnInit, inject, signal, viewChild } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { PerfilCliente } from '../../../core/models/cliente.model';
import { ProblemDetail } from '../../../core/models/problem.model';
import { ClienteService } from '../../../core/services/cliente.service';
import { MascaraDirective } from '../../../shared/directives/mascara.directive';
import { somenteDigitos } from '../../../core/utils/documentos';

/** "Minha conta > Perfil e dados" (protótipo 02, conta-perfil.html). */
@Component({
  selector: 'app-conta-perfil',
  imports: [AccountMenu, DatePipe, ReactiveFormsModule, RouterLink, MascaraDirective],
  providers: [ContaSessao],
  templateUrl: './conta-perfil.html',
  styleUrl: './conta-perfil.scss',
})
export class ContaPerfil implements OnInit {
  protected readonly sessao = inject(ContaSessao);
  private readonly clientes = inject(ClienteService);
  private readonly fb = inject(FormBuilder);
  private readonly dialogoSenha = viewChild.required<ElementRef<HTMLDialogElement>>('dlgSenha');

  private readonly auth = inject(AuthService);
  readonly perfil = signal<PerfilCliente | null>(null);
  readonly sessoes = signal<SessaoUsuario[]>([]);
  readonly salvando = signal(false);
  readonly mensagem = signal<{ texto: string; ok: boolean } | null>(null);
  readonly mensagemSenha = signal<{ texto: string; ok: boolean } | null>(null);

  readonly form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(120)]],
    telefone: [''],
  });

  readonly formSenha = this.fb.nonNullable.group({
    senhaAtual: ['', Validators.required],
    senhaNova: ['', [Validators.required, Validators.minLength(8)]],
    confirmar: ['', Validators.required],
  });

  ngOnInit(): void {
    this.auth.sessoes().subscribe((s) => this.sessoes.set(s));
    this.clientes.perfil().subscribe((p) => {
      this.perfil.set(p);
      this.sessao.resumo.set({ nome: p.nome, detalhe: p.email });
      this.form.patchValue({ nome: p.nome ?? '', telefone: p.telefone ?? '' });
    });
  }

  /** Documento mascarado: só o miolo aparece (o documento não muda pela tela). */
  documentoOculto(): string {
    const d = this.perfil()?.documento ?? '';
    if (d.length === 11) return `•••.${d.slice(3, 6)}.${d.slice(6, 9)}-••`;
    if (d.length === 14) return `••.${d.slice(2, 5)}.${d.slice(5, 8)}/${d.slice(8, 12)}-••`;
    return d;
  }

  salvar(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.salvando()) return;
    const v = this.form.getRawValue();
    this.salvando.set(true);
    this.clientes
      .alterarPerfil({ nome: v.nome.trim(), telefone: somenteDigitos(v.telefone) })
      .subscribe({
        next: () => {
          this.salvando.set(false);
          this.perfil.update((p) => (p ? { ...p, nome: v.nome.trim() } : p));
          this.sessao.renomear(v.nome.trim());
          this.mensagem.set({ texto: 'Dados salvos.', ok: true });
        },
        error: (e: HttpErrorResponse) => {
          this.salvando.set(false);
          this.mensagem.set({ texto: this.textoErro(e), ok: false });
        },
      });
  }

  /** Encerra outra sessão da conta (#20): o token dela deixa de valer. */
  encerrarSessao(id: number): void {
    this.auth
      .revogarSessao(id)
      .subscribe(() => this.sessoes.update((lista) => lista.filter((s) => s.id !== id)));
  }

  abrirSenha(): void {
    this.formSenha.reset();
    this.mensagemSenha.set(null);
    this.dialogoSenha().nativeElement.showModal();
  }

  fecharSenha(): void {
    this.dialogoSenha().nativeElement.close();
  }

  trocarSenha(): void {
    this.formSenha.markAllAsTouched();
    const v = this.formSenha.getRawValue();
    if (this.formSenha.invalid) {
      this.mensagemSenha.set({
        texto: 'A nova senha precisa de pelo menos 8 caracteres.',
        ok: false,
      });
      return;
    }
    if (v.senhaNova !== v.confirmar) {
      this.mensagemSenha.set({ texto: 'As senhas não conferem.', ok: false });
      return;
    }
    this.clientes.trocarSenha({ senhaAtual: v.senhaAtual, senhaNova: v.senhaNova }).subscribe({
      next: () => {
        this.fecharSenha();
        this.mensagem.set({ texto: 'Senha atualizada.', ok: true });
      },
      error: (e: HttpErrorResponse) =>
        this.mensagemSenha.set({ texto: this.textoErro(e), ok: false }),
    });
  }

  private textoErro(e: HttpErrorResponse): string {
    const p = e.error as ProblemDetail | null;
    return p?.errors?.[0]?.message ?? p?.detail ?? 'Não foi possível salvar. Tente novamente.';
  }
}
